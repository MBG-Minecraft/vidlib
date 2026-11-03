package dev.latvian.mods.vidlib.feature.zone;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;

import java.util.List;

public class ZoneEvent extends Event {
	public static class EntityEvent extends ZoneEvent {
		private final ResolvedZone resolvedZone;
		private final Level level;
		private final Entity entity;

		public EntityEvent(ResolvedZone resolvedZone, Level level, Entity entity) {
			this.resolvedZone = resolvedZone;
			this.level = level;
			this.entity = entity;
		}

		public ResolvedZone getResolvedZone() {
			return resolvedZone;
		}

		public Zone getZone() {
			return resolvedZone.zone;
		}

		public CompoundTag getData() {
			return resolvedZone.zone.data();
		}

		public Level getLevel() {
			return level;
		}

		public Entity getEntity() {
			return entity;
		}
	}

	public static class EntityEntered extends EntityEvent {
		public EntityEntered(ResolvedZone zone, Level level, Entity entity) {
			super(zone, level, entity);
		}
	}

	public static class EntityExited extends EntityEvent {
		public EntityExited(ResolvedZone zone, Level level, Entity entity) {
			super(zone, level, entity);
		}
	}

	public static class ClickedOn extends EntityEvent {
		private final ZoneHit hit;

		public ClickedOn(ZoneHit hit, Level level, Entity entity) {
			super(hit.resolvedZone(), level, entity);
			this.hit = hit;
		}

		public ZoneHit getHit() {
			return hit;
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
