package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerMinecraftServer;
import dev.beast.mods.shimmer.feature.clock.ClockInstance;
import dev.beast.mods.shimmer.feature.zone.ZoneLoader;
import dev.beast.mods.shimmer.util.ScheduledTask;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements ShimmerMinecraftServer {
	@Shadow
	@Final
	private Map<ResourceKey<Level>, ServerLevel> levels;
	@Unique
	private ScheduledTask.Handler shimmer$scheduledTaskHandler;

	@Unique
	private ServerLevel shimmer$overworld;

	@Override
	public ScheduledTask.Handler shimmer$getScheduledTaskHandler() {
		if (shimmer$scheduledTaskHandler == null) {
			shimmer$scheduledTaskHandler = new ScheduledTask.Handler(shimmer$self(), () -> shimmer$self().overworld());
		}

		return shimmer$scheduledTaskHandler;
	}

	@Override
	public void shimmer$playerJoined(ServerPlayer player) {
		player.shimmer$sessionData().loadPlayerData(shimmer$self());
	}

	@Override
	public void shimmer$preTick() {
		for (var level : shimmer$self().getAllLevels()) {
			var zones = ZoneLoader.BY_DIMENSION.get(level.dimension());
			level.shimmer$setActiveZones(zones);

			if (zones != null) {
				zones.entityZones.clear();

				for (var container : zones) {
					container.tick(zones, level);
				}
			}
		}

		for (var instance : ClockInstance.SERVER.getMap().values()) {
			instance.tick(shimmer$self().getLevel(instance.clock.dimension()));
		}
	}

	@Override
	public void shimmer$postTick() {
		if (shimmer$scheduledTaskHandler != null) {
			shimmer$scheduledTaskHandler.tick();
		}

		for (var player : shimmer$self().getPlayerList().getPlayers()) {
			player.shimmer$sessionData().syncPlayerData(player);
		}
	}

	@Inject(method = "createLevels", at = @At("RETURN"))
	private void shimmer$createLevels(CallbackInfo ci) {
		shimmer$overworld = levels.get(Level.OVERWORLD);
	}

	/**
	 * @author Lat
	 * @reason Optimization
	 */
	@Overwrite
	public final ServerLevel overworld() {
		return shimmer$overworld;
	}
}
