package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.klib.util.Side;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;

import java.util.List;

public class ZoneEvent extends Event {
	public static class EntityEvent extends ZoneEvent {
		private final ZoneInstance zoneInstance;
		private final Level level;
		private final Entity entity;

		public EntityEvent(ZoneInstance zoneInstance, Level level, Entity entity) {
			this.zoneInstance = zoneInstance;
			this.level = level;
			this.entity = entity;
		}

		public ZoneInstance getZoneInstance() {
			return zoneInstance;
		}

		public CompoundTag getData() {
			return zoneInstance.zone.data();
		}

		public Level getLevel() {
			return level;
		}

		public Entity getEntity() {
			return entity;
		}
	}

	public static class EntityEntered extends EntityEvent {
		public EntityEntered(ZoneInstance zoneInstance, Level level, Entity entity) {
			super(zoneInstance, level, entity);
		}
	}

	public static class EntityExited extends EntityEvent {
		public EntityExited(ZoneInstance zoneInstance, Level level, Entity entity) {
			super(zoneInstance, level, entity);
		}
	}

	public static class ClickedOn extends EntityEvent {
		private final ZoneClipResult clip;

		public ClickedOn(ZoneClipResult clip, Level level, Entity entity) {
			super(clip.instance(), level, entity);
			this.clip = clip;
		}

		public ZoneClipResult getClip() {
			return clip;
		}
	}

	public static class Generate extends ZoneEvent {
		private final List<ZoneContainer> zoneContainers;

		public Generate(List<ZoneContainer> zoneContainers) {
			this.zoneContainers = zoneContainers;
		}

		public void add(ZoneContainer zoneContainer) {
			zoneContainers.add(zoneContainer);
			zoneContainer.generated = true;
		}
	}

	public static class Updated extends ZoneEvent {
		private final ResourceKey<Level> dimension;
		private final ActiveZones zones;
		private final Side side;

		public Updated(ResourceKey<Level> dimension, ActiveZones zones, Side side) {
			this.dimension = dimension;
			this.zones = zones;
			this.side = side;
		}

		public ResourceKey<Level> getDimension() {
			return dimension;
		}

		public ActiveZones getZones() {
			return zones;
		}

		public Side getSide() {
			return side;
		}
	}
}
