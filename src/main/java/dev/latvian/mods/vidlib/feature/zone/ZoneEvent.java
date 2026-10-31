package dev.latvian.mods.vidlib.feature.zone;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;

import java.util.List;

public class ZoneEvent extends Event {
	public static class EntityEvent extends ZoneEvent {
		private final Zone zone;
		private final Level level;
		private final Entity entity;

		public EntityEvent(Zone zone, Level level, Entity entity) {
			this.zone = zone;
			this.level = level;
			this.entity = entity;
		}

		public Zone getZone() {
			return zone;
		}

		public CompoundTag getData() {
			return zone.volume.data();
		}

		public Level getLevel() {
			return level;
		}

		public Entity getEntity() {
			return entity;
		}
	}

	public static class EntityEntered extends EntityEvent {
		public EntityEntered(Zone zone, Level level, Entity entity) {
			super(zone, level, entity);
		}
	}

	public static class EntityExited extends EntityEvent {
		public EntityExited(Zone zone, Level level, Entity entity) {
			super(zone, level, entity);
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
}
