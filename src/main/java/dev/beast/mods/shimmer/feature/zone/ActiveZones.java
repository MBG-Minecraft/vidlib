package dev.beast.mods.shimmer.feature.zone;

import dev.beast.mods.shimmer.feature.entity.filter.EntityFilter;
import dev.beast.mods.shimmer.math.Line;
import dev.beast.mods.shimmer.math.VoxelShapeBox;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ActiveZones implements Iterable<ZoneContainer> {
	public record SolidZone(ZoneInstance instance, VoxelShape shape, VoxelShapeBox shapeBox) {
	}

	public static final StreamCodec<RegistryFriendlyByteBuf, ActiveZones> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public ActiveZones decode(RegistryFriendlyByteBuf buf) {
			int count = buf.readVarInt();
			var active = new ActiveZones();

			for (int i = 0; i < count; i++) {
				var container = ZoneContainer.DIRECT_STREAM_CODEC.decode(buf);
				active.containers.put(container.id, container);
			}

			return active;
		}

		@Override
		public void encode(RegistryFriendlyByteBuf buf, ActiveZones value) {
			buf.writeVarInt(value.containers.size());

			for (var container : value) {
				ZoneContainer.DIRECT_STREAM_CODEC.encode(buf, container);
			}
		}
	};

	public final Map<ResourceLocation, ZoneContainer> containers;
	public final Int2ObjectOpenHashMap<List<ZoneInstance>> entityZones;
	List<SolidZone> solidZones;

	public ActiveZones() {
		this.containers = new LinkedHashMap<>();
		this.entityZones = new Int2ObjectOpenHashMap<>();
		this.solidZones = null;
	}

	@Nullable
	public ZoneContainer get(ResourceLocation id) {
		return containers.get(id);
	}

	public void filter(ResourceKey<Level> dimension, ActiveZones from) {
		containers.clear();
		entityZones.clear();

		for (var container : from) {
			if (container.dimension == dimension) {
				containers.put(container.id, container);
			}
		}

		solidZones = null;
	}

	public void update(Collection<ZoneContainer> zones) {
		containers.clear();

		for (var zone : zones) {
			zone.parent = this;
			containers.put(zone.id, zone);
		}

		solidZones = null;
	}

	@Override
	@NotNull
	public Iterator<ZoneContainer> iterator() {
		return containers.values().iterator();
	}

	public void tick(Level level) {
		entityZones.clear();

		for (var container : this) {
			container.tick(this, level);
		}

		for (var player : level.players()) {
			var session = player.shimmer$sessionData();
			session.zonesIn = entityZones.getOrDefault(player.getId(), List.of());
			session.zonesTagsIn = Set.of();

			if (!session.zonesIn.isEmpty()) {
				session.zonesTagsIn = new LinkedHashSet<>(session.zonesIn.size());

				for (var zone : session.zonesIn) {
					session.zonesTagsIn.addAll(zone.tags);
				}
			}
		}
	}

	@Nullable
	public ZoneClipResult clip(Line ray) {
		ZoneClipResult result = null;

		for (var container : this) {
			var clip = container.clip(ray);

			if (clip != null) {
				if (result == null || clip.distanceSq() < result.distanceSq()) {
					result = clip;
				}
			}
		}

		return result;
	}

	public List<SolidZone> getSolidZones() {
		if (solidZones == null) {
			solidZones = new ArrayList<>(0);

			for (var container : containers.values()) {
				for (var zone : container.zones) {
					if (zone.zone.solid() != EntityFilter.NONE.instance()) {
						var shape = zone.zone.shape().createVoxelShape().optimize();

						if (!shape.isEmpty()) {
							solidZones.add(new SolidZone(zone, shape, VoxelShapeBox.of(shape)));
						}
					}
				}
			}

			solidZones = List.copyOf(solidZones);
		}

		return solidZones;
	}

	public boolean intersectsSolid(@Nullable Entity entity, AABB collisionBox) {
		if (entity == null) {
			return false;
		}

		var solidZones = getSolidZones();

		if (solidZones.isEmpty()) {
			return false;
		}

		for (var sz : solidZones) {
			if (sz.instance.zone.solid().test(entity)) {
				for (var box : sz.shapeBox.boxes()) {
					if (box.intersects(collisionBox)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public List<VoxelShape> getShapesIntersecting(@Nullable Entity entity, AABB collisionBox) {
		if (entity == null) {
			return List.of();
		}

		var solidZones = getSolidZones();

		if (solidZones.isEmpty()) {
			return List.of();
		}

		var shapes = new ArrayList<VoxelShape>(0);

		for (var sz : solidZones) {
			if (sz.instance.zone.solid().test(entity)) {
				for (var box : sz.shapeBox.boxes()) {
					if (box.intersects(collisionBox)) {
						shapes.add(sz.shape);
						break;
					}
				}
			}
		}

		return shapes;
	}
}