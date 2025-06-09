package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.klib.math.Line;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.Vec3i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
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
import java.util.UUID;

public class ActiveZones implements Iterable<ZoneContainer> {
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
	CachedZoneShape[] visible;
	CachedZoneShape[] solidZones;
	CachedZoneShape[] fluidZones;

	public ActiveZones() {
		this.containers = new LinkedHashMap<>();
		this.entityZones = new Int2ObjectOpenHashMap<>();
	}

	public void clearCache() {
		visible = null;
		solidZones = null;
		fluidZones = null;
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

		clearCache();
	}

	public void update(Collection<ZoneContainer> zones) {
		containers.clear();

		for (var zone : zones) {
			zone.parent = this;
			containers.put(zone.id, zone);
		}

		clearCache();
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
			var session = player.vl$sessionData();
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

	public CachedZoneShape[] getVisible() {
		if (visible == null) {
			var list = new ArrayList<CachedZoneShape>(0);

			for (var container : containers.values()) {
				for (var zone : container.zones) {
					if (zone.zone.isVisible()) {
						CachedZoneShape.append(list, zone);
					}
				}
			}

			visible = list.toArray(CachedZoneShape.EMPTY_ARRAY);
		}

		return visible;
	}

	public CachedZoneShape[] getSolidZones() {
		if (solidZones == null) {
			var list = new ArrayList<CachedZoneShape>(0);

			for (var container : containers.values()) {
				for (var zone : container.zones) {
					if (zone.zone.isSolid()) {
						CachedZoneShape.append(list, zone);
					}
				}
			}

			solidZones = list.toArray(CachedZoneShape.EMPTY_ARRAY);
		}

		return solidZones;
	}

	public boolean intersectsSolid(@Nullable Entity entity, AABB collisionBox) {
		if (entity == null) {
			return false;
		}

		for (var sz : getSolidZones()) {
			if (sz.instance().zone.solid().test(entity)) {
				for (var box : sz.shapeBox().boxes()) {
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

		if (solidZones.length == 0) {
			return List.of();
		}

		var shapes = new ArrayList<VoxelShape>(0);

		for (var sz : solidZones) {
			if (sz.instance().zone.solid().test(entity)) {
				for (var box : sz.shapeBox().boxes()) {
					if (box.intersects(collisionBox)) {
						shapes.add(sz.shape());
						break;
					}
				}
			}
		}

		return shapes;
	}

	public void remove(UUID uuid) {
		for (var container : containers.values()) {
			container.remove(uuid);
		}

		clearCache();
	}

	public CachedZoneShape[] getFluidZones() {
		if (fluidZones == null) {
			var list = new ArrayList<CachedZoneShape>(0);

			for (var container : containers.values()) {
				for (var zone : container.zones) {
					if (!zone.zone.fluid().isEmpty()) {
						CachedZoneShape.append(list, zone);
					}
				}
			}

			fluidZones = list.toArray(CachedZoneShape.EMPTY_ARRAY);
		}

		return fluidZones;
	}

	@Nullable
	public FluidState getZoneFluidState(Vec3i pos) {
		for (var c : getFluidZones()) {
			if (c.instance().zone.shape().contains(pos)) {
				return c.instance().zone.fluid().fluidState();
			}
		}

		return null;
	}

	public float getZoneFluidHeight(FlowingFluid fluid, Vec3i pos) {
		var above = getZoneFluidState(pos.above());

		if (above != null && fluid.isSame(above.getType())) {
			return 1F;
		}

		return 0F;
	}
}