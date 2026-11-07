package dev.latvian.mods.vidlib.feature.zone;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;

import java.util.function.BiConsumer;

public class ZoneEvent extends Event {
	public static class EntityEvent extends ZoneEvent {
		private final ResolvedZone zone;
		private final Level level;
		private final Entity entity;

		public EntityEvent(ResolvedZone zone, Level level, Entity entity) {
			this.zone = zone;
			this.level = level;
			this.entity = entity;
		}

		public ResolvedZone getZone() {
			return zone;
		}

		public CompoundTag getData() {
			return zone.data;
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
		private final Player player;
		private final ZoneHit hit;

		public ClickedOn(Player player, ZoneHit hit) {
			super(hit.resolvedZone(), player.level(), player);
			this.player = player;
			this.hit = hit;
		}

		public Player getPlayer() {
			return player;
		}

		public ZoneHit getHit() {
			return hit;
		}
	}

	public static class Generate extends ZoneEvent {
		private final BiConsumer<String, Zone> callback;

		public Generate(BiConsumer<String, Zone> callback) {
			this.callback = callback;
		}

		public void add(String id, Zone zone) {
			callback.accept(id, zone);
		}
	}
}
