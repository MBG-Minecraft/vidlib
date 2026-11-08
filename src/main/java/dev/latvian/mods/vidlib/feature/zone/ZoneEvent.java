package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.registry.RefType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public class ZoneEvent extends Event {
	public static class EntityEvent extends ZoneEvent {
		private final Ref<Zone> zone;
		private final ResolvedZone resolvedZone;
		private final Level level;
		private final Entity entity;

		public EntityEvent(Ref<Zone> zone, @Nullable ResolvedZone resolvedZone, Level level, Entity entity) {
			this.zone = zone;
			this.resolvedZone = resolvedZone;
			this.level = level;
			this.entity = entity;
		}

		public Ref<Zone> getZone() {
			return zone;
		}

		@Nullable
		public ResolvedZone getResolvedZone() {
			return resolvedZone;
		}

		public CompoundTag getData() {
			return resolvedZone == null ? zone.value().data() : resolvedZone.data;
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
			super(zone.ref, zone, level, entity);
		}
	}

	public static class EntityExited extends EntityEvent {
		public EntityExited(ResolvedZone zone, Level level, Entity entity) {
			super(zone.ref, zone, level, entity);
		}
	}

	public static class ClickedOn extends EntityEvent {
		private final Player player;
		private final ZoneHit hit;

		public ClickedOn(Player player, ZoneHit hit) {
			super(hit.zone(), hit.resolvedZone(), player.level(), player);
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
		private final RefType refType;

		public Generate(BiConsumer<String, Zone> callback, RefType refType) {
			this.callback = callback;
			this.refType = refType;
		}

		public RefType getRefType() {
			return refType;
		}

		public void add(String id, Zone zone) {
			callback.accept(id, zone);
		}
	}
}
