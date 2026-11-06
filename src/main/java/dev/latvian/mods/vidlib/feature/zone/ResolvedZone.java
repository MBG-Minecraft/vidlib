package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.entity.filter.AnyEntityFilter;
import dev.latvian.mods.klib.entity.filter.EntityFilter;
import dev.latvian.mods.klib.entity.filter.NoneEntityFilter;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.texture.CubeTextureMap;
import dev.latvian.mods.klib.texture.CubeTexturePatch;
import dev.latvian.mods.klib.texture.FaceTexture;
import dev.latvian.mods.klib.texture.LightUV;
import dev.latvian.mods.klib.texture.OverlayUV;
import dev.latvian.mods.klib.texture.SpriteKey;
import dev.latvian.mods.klib.visual.TexturedCubeVisual;
import dev.latvian.mods.klib.visual.Visual;
import dev.latvian.mods.klib.volume.Volume;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ResolvedZone implements Comparable<ResolvedZone> {
	public final LevelZones levelZones;
	public final Ref<Zone> ref;
	public final int priority;
	public final boolean generated;
	public final Volume volume;
	public final EntityFilter entityFilter;
	public final CompoundTag data;
	public final EntityFilter solid;
	public final Set<ZoneTag> tags;
	public final boolean anchor;
	public final ZoneFluid fluid;
	public final Color color;

	public final Int2ObjectMap<Entity> entities;
	public final Int2ObjectMap<Entity> oldEntities;
	public final VoxelShape voxelShape;
	public final List<AABB> boxes;
	public final List<Visual> visuals;

	public ResolvedZone(LevelZones levelZones, Ref<Zone> zoneRef) {
		this.levelZones = levelZones;
		this.ref = zoneRef;
		var zone = zoneRef.value();
		this.priority = zone.priority();
		this.generated = zone.generated();
		this.volume = zone.volume().value();
		this.entityFilter = zone.entityFilter().value();
		this.data = zone.data().copy();
		this.solid = zone.solid().value();
		this.tags = new ReferenceArraySet<>(zone.tags());
		this.anchor = zone.anchor();
		this.fluid = zone.fluid().orElse(null);
		this.color = zone.display().color();

		this.entities = new Int2ObjectOpenHashMap<>();
		this.oldEntities = new Int2ObjectOpenHashMap<>();
		this.voxelShape = volume.createVoxelShape().optimize();
		this.boxes = List.copyOf(voxelShape.toAabbs());
		this.visuals = new ArrayList<>();

		double yOff = 0D;

		if (fluid != null) {
			yOff = 1D - fluid.fluidState().getOwnHeight();
			// yOff += Math.clamp(-frame.y(yOff) / 50D, 0D, 0.5D);
		}

		for (var box : boxes) {
			var bminX = box.minX;
			var bminY = box.minY;
			var bminZ = box.minZ;
			var bmaxX = box.maxX;
			var bmaxY = box.maxY - yOff;
			var bmaxZ = box.maxZ;

			var textures = CubeTextureMap.EMPTY;

			if (fluid != null) {
				textures = textures.merge(fluid.textures().cubeTextures());
			}

			textures = textures.merge(zone.display().textures());

			if (textures != CubeTextureMap.EMPTY) {
				visuals.add(new TexturedCubeVisual(new AABB(bminX, bminY, bminZ, bmaxX, bmaxY, bmaxZ), textures, LightUV.FULL_BRIGHT, OverlayUV.NORMAL));
			}

			var fog = zone.display().fog();

			// directionless fog not supported yet
			if (!fog.isNone() && fog.direction().isPresent()) {
				var dir = fog.direction().get();

				var tex = CubeTextureMap.EMPTY.merge(CubeTexturePatch.of(dir, new FaceTexture(SpriteKey.WHITE, false, fog.color(), 0F, 1D)));

				for (int i = 0; i <= fog.steps(); i++) {
					double l = i / (double) fog.steps();

					switch (dir) {
						case DOWN, UP -> {
							double y = KMath.lerp(dir == Direction.UP ? (1D - l) : l, bminY, bmaxY);
							visuals.add(new TexturedCubeVisual(new AABB(bminX, y, bminZ, bmaxX, y, bmaxZ), tex, LightUV.FULL_BRIGHT, OverlayUV.NORMAL));
						}
						case NORTH, SOUTH -> {
							double z = KMath.lerp(dir == Direction.SOUTH ? (1D - l) : l, bminZ, bmaxZ);
							visuals.add(new TexturedCubeVisual(new AABB(bminX, bminY, z, bmaxX, bmaxY, z), tex, LightUV.FULL_BRIGHT, OverlayUV.NORMAL));
						}
						case WEST, EAST -> {
							double x = KMath.lerp(dir == Direction.EAST ? (1D - l) : l, bminX, bmaxX);
							visuals.add(new TexturedCubeVisual(new AABB(x, bminY, bminZ, x, bmaxY, bmaxZ), tex, LightUV.FULL_BRIGHT, OverlayUV.NORMAL));
						}
					}
				}
			}
		}
	}

	public boolean has(Entity entity) {
		return entities.containsKey(entity.getId());
	}

	public boolean is(ZoneTag tag) {
		return tags.contains(tag);
	}

	public void tick(Level level, Iterable<Entity> allEntities) {
		oldEntities.clear();
		oldEntities.putAll(entities);
		entities.clear();

		for (var entity : allEntities) {
			if (entity.isAlive() && volume.intersects(entity.getBoundingBox()) && entityFilter.test(entity)) {
				entities.put(entity.getId(), entity);

				var list = levelZones.entityZones.get(entity.getId());

				if (list == null) {
					list = new ArrayList<>(1);
					levelZones.entityZones.put(entity.getId(), list);
				}

				list.add(this);

				if (!oldEntities.containsKey(entity.getId())) {
					VLPlatformHelper.INSTANCE.entityEnteredZone(this, level, entity);
				}
			}
		}

		for (var old : oldEntities.values()) {
			if (!entities.containsKey(old.getId())) {
				VLPlatformHelper.INSTANCE.entityExitedZone(this, level, old);
			}
		}
	}

	@Override
	public int compareTo(@NotNull ResolvedZone other) {
		int i = Integer.compare(other.priority, priority);
		return i == 0 ? ref.key().compareTo(other.ref.key()) : i;
	}

	public boolean isVisible() {
		return isSolid() || fluid != null || !visuals.isEmpty();
	}

	public boolean isSolid() {
		return solid != NoneEntityFilter.INSTANCE;
	}

	public boolean isFluid() {
		return fluid != null;
	}

	public boolean collidesWith(@Nullable Entity entity) {
		return solid == AnyEntityFilter.INSTANCE || entity != null && solid.test(entity);
	}
}
