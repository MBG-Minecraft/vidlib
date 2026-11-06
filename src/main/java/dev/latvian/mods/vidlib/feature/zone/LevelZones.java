package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.klib.math.HitBuilder;
import dev.latvian.mods.klib.registry.Ref;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LevelZones implements Iterable<ResolvedZone> {
	public final ResourceKey<Level> dimension;
	public final List<ResolvedZone> zones;
	public final Int2ObjectOpenHashMap<List<ResolvedZone>> entityZones;
	public final List<ResolvedZone> visibleZones;
	public final List<ResolvedZone> solidZones;
	public final List<ResolvedZone> fluidZones;

	public LevelZones(ResourceKey<Level> dimension, List<ResolvedZone> zones) {
		this.dimension = dimension;
		this.zones = List.copyOf(zones);
		this.entityZones = new Int2ObjectOpenHashMap<>();
		this.visibleZones = List.copyOf(this.zones.stream().filter(ResolvedZone::isVisible).toList());
		this.solidZones = List.copyOf(this.zones.stream().filter(ResolvedZone::isSolid).toList());
		this.fluidZones = List.copyOf(this.zones.stream().filter(ResolvedZone::isFluid).toList());
	}

	@Override
	public @NotNull Iterator<ResolvedZone> iterator() {
		return zones.iterator();
	}

	@Nullable
	public ResolvedZone getResolved(Ref<Zone> ref) {
		for (var zone : zones) {
			if (zone.ref.equals(ref)) {
				return zone;
			}
		}

		return null;
	}

	public void tick(Level level, Iterable<Entity> allEntities) {
		entityZones.clear();

		for (var instance : zones) {
			instance.tick(level, allEntities);
		}

		for (var entry : entityZones.int2ObjectEntrySet()) {
			var list = entityZones.get(entry.getIntKey());

			if (list == null) {
				list = new ArrayList<>(entry.getValue().size());
				entityZones.put(entry.getIntKey(), list);
			}

			list.addAll(entry.getValue());
		}
	}

	@Nullable
	public ResolvedZone getFirst(Vec3 pos) {
		for (var instance : zones) {
			if (instance.volume.contains(pos)) {
				return instance;
			}
		}

		return null;
	}

	public List<ResolvedZone> getAll(Vec3 pos) {
		var list = new ArrayList<ResolvedZone>(1);

		for (var instance : zones) {
			if (instance.volume.contains(pos)) {
				list.add(instance);
			}
		}

		return list;
	}

	@Nullable
	public ResolvedZone getFirst(AABB box) {
		for (var instance : zones) {
			if (instance.volume.intersects(box)) {
				return instance;
			}
		}

		return null;
	}

	public List<ResolvedZone> getAll(AABB box) {
		var list = new ArrayList<ResolvedZone>(1);

		for (var instance : zones) {
			if (instance.volume.intersects(box)) {
				list.add(instance);
			}
		}

		return list;
	}

	@Nullable
	public ZoneHit hit(HitBuilder hit) {
		ZoneHit result = null;

		for (var zone : zones) {
			if (zone.volume.hit(hit) && hit.hit != null && (result == null || hit.distanceSq < result.distanceSq())) {
				result = new ZoneHit(zone.ref, zone, hit.hit, hit.distanceSq, hit.direction);
			}
		}

		return result;
	}

	@Nullable
	public ZoneHit hitLevel(HitBuilder hit) {
		ZoneHit result = null;

		for (var zone : fluidZones) {
			if (zone.volume.hit(hit) && hit.hit != null && (result == null || hit.distanceSq < result.distanceSq())) {
				result = new ZoneHit(zone.ref, zone, hit.hit, hit.distanceSq, hit.direction);
			}
		}

		return result;
	}

	public boolean intersectsSolid(@Nullable Entity entity, AABB collisionBox) {
		if (solidZones.isEmpty()) {
			return false;
		}

		for (var zone : solidZones) {
			if (zone.collidesWith(entity)) {
				if (zone.volume.intersects(collisionBox)) {
					return true;
				}
			}
		}

		return false;
	}

	public void collectShapesIntersecting(@Nullable Entity entity, AABB collisionBox, List<VoxelShape> shapes) {
		if (solidZones.isEmpty()) {
			return;
		}

		for (var zone : solidZones) {
			if (zone.collidesWith(entity)) {
				for (var box : zone.boxes) {
					if (box.intersects(collisionBox)) {
						if (shapes.isEmpty()) {
							shapes = new ArrayList<>(1);
						}

						shapes.add(zone.voxelShape);
						break;
					}
				}
			}
		}
	}
}
