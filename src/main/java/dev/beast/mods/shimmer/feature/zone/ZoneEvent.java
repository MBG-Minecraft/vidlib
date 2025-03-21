package dev.beast.mods.shimmer.feature.zone;

import dev.beast.mods.shimmer.util.Side;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;

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
