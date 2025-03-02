package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerMinecraftServer;
import dev.beast.mods.shimmer.feature.zone.ZoneReloadListener;
import dev.beast.mods.shimmer.util.ScheduledTask;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements ShimmerMinecraftServer {
	@Shadow
	public abstract void endMetricsRecordingTick();

	@Shadow
	public abstract boolean enforceSecureProfile();

	@Unique
	private ScheduledTask.Handler shimmer$scheduledTaskHandler;

	@Override
	public ScheduledTask.Handler shimmer$getScheduledTaskHandler() {
		if (shimmer$scheduledTaskHandler == null) {
			shimmer$scheduledTaskHandler = new ScheduledTask.Handler(shimmer$self(), () -> shimmer$self().overworld());
		}

		return shimmer$scheduledTaskHandler;
	}

	@Override
	public void shimmer$playerJoined(ServerPlayer player) {
	}

	@Override
	public void shimmer$postTick() {
		if (shimmer$scheduledTaskHandler != null) {
			shimmer$scheduledTaskHandler.tick();
		}

		for (var level : shimmer$self().getAllLevels()) {
			var zones = ZoneReloadListener.BY_DIMENSION.get(level.dimension());
			level.shimmer$setActiveZones(zones);

			if (zones != null) {
				zones.entityZones.clear();

				for (var container : zones) {
					container.tick(zones, level);
				}
			}
		}
	}
}
