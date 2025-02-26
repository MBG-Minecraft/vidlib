package dev.beast.mods.shimmer.feature.zone;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;

import java.util.Objects;
import java.util.function.Consumer;

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

	public static class Refresh extends ZoneEvent {
		private final MinecraftServer server;
		private final Consumer<ZoneContainer> zones;

		public Refresh(MinecraftServer server, Consumer<ZoneContainer> zones) {
			this.server = server;
			this.zones = zones;
		}

		public MinecraftServer getServer() {
			return server;
		}

		public void set(ZoneContainer zones) {
			this.zones.accept(Objects.requireNonNull(zones));
		}

		public void set(ResourceLocation dataId) {
			var container = ZoneContainer.SERVER.get(dataId);

			if (container == null) {
				throw new IllegalArgumentException("Zone container file with ID '" + dataId + "' not found!");
			} else {
				zones.accept(container);
			}
		}
	}
}
