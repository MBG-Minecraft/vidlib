package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.shape.VoxelShapeBox;
import dev.latvian.mods.vidlib.feature.zone.shape.ZoneShape;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ZoneCache implements Iterable<ZoneContainer> {
	public static final Map<ResourceKey<Level>, ZoneCache> CLIENT = new Reference2ObjectOpenHashMap<>(3);
	public static final Map<ResourceKey<Level>, ZoneCache> SERVER = new Reference2ObjectOpenHashMap<>(3);

	public static ZoneCache of(ResourceKey<Level> dimension, boolean client) {
		var map = client ? CLIENT : SERVER;
		var cache = map.get(dimension);

		if (cache == null) {
			cache = new ZoneCache(dimension);

			for (var ref : ZoneContainer.REGISTRY) {
				var container = ref.value();

				if (container.dimension == dimension) {
					cache.containers.put(container.ref().key(), container);
				}
			}

			map.put(dimension, cache);
		}

		return cache;
	}

	public static void clearAll() {
		CLIENT.clear();
		SERVER.clear();
	}

	public final ResourceKey<Level> dimension;
	public final Int2ObjectOpenHashMap<List<Zone>> entityZones;
	public final Map<ZoneShape, VoxelShapeBox> cachedZoneShapes;
	CachedZoneShape[] visible;
	CachedZoneShape[] solidZones;
	CachedZoneShape[] fluidZones;

	private ZoneCache(ResourceKey<Level> dimension) {
		this.dimension = dimension;
		this.entityZones = new Int2ObjectOpenHashMap<>();
		this.cachedZoneShapes = new Reference2ObjectOpenHashMap<>();
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
	public ZoneClipResult clip(ClipContext ctx) {
		ZoneClipResult result = null;

		for (var container : this) {
			var clip = container.clip(ctx);

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
					if (zone.volume.isVisible()) {
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
					if (zone.volume.isSolid()) {
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
			if (sz.instance().volume.solid().value().test(entity)) {
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

		var shapes = List.<VoxelShape>of();

		for (var sz : solidZones) {
			if (sz.instance().volume.solid().value().test(entity)) {
				for (var box : sz.shapeBox().boxes()) {
					if (box.intersects(collisionBox)) {
						if (shapes.isEmpty()) {
							shapes = new ArrayList<>();
						}

						shapes.add(sz.shape());
						break;
					}
				}
			}
		}

		return shapes;
	}

	@ApiStatus.Internal
	public void remove(Ref<ZoneContainer> zone, int index) {
		var container = zone.value();

		if (container != null) {
			container.remove(index);
		}

		clearCache();
	}

	@ApiStatus.Internal
	public void update(Ref<ZoneContainer> zone, int index, ZoneVolume zoneVolume) {
		var container = containers.get(zone);

		if (container != null) {
			container.update(index, zoneVolume);
		}

		clearCache();
	}

	public CachedZoneShape[] getFluidZones() {
		if (fluidZones == null) {
			var list = new ArrayList<CachedZoneShape>(0);

			for (var container : ZoneContainer.REGISTRY) {
				for (var zone : container.zones) {
					if (zone.volume.fluid().isPresent()) {
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
			if (c.instance().volume.fluid().isPresent() && c.instance().volume.shape().value().contains(pos)) {
				return c.instance().volume.fluid().get().fluidState();
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

	@Nullable
	public ZoneClipResult clipLevel(ClipContext ctx) {
		ZoneClipResult result = null;

		for (var z : getFluidZones()) {
			var clip = z.instance().volume.shape().value().clip(new ZoneClipContext(z.instance(), ctx));

			if (clip != null) {
				if (result == null || clip.distanceSq() < result.distanceSq()) {
					result = clip;
				}
			}
		}

		return result;
	}
}