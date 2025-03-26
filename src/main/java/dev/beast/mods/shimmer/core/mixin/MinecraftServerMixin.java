package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerMinecraftServer;
import dev.beast.mods.shimmer.feature.clock.ClockValue;
import dev.beast.mods.shimmer.feature.data.DataMap;
import dev.beast.mods.shimmer.feature.data.DataType;
import dev.beast.mods.shimmer.util.PauseType;
import dev.beast.mods.shimmer.util.ScheduledTask;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
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

	@Unique
	private DataMap shimmer$serverDataMap;

	@Unique
	private final Map<ResourceLocation, ClockValue> shimmer$clocks = new HashMap<>();

	@Override
	public ScheduledTask.Handler shimmer$getScheduledTaskHandler() {
		if (shimmer$scheduledTaskHandler == null) {
			shimmer$scheduledTaskHandler = new ScheduledTask.Handler(shimmer$self(), () -> shimmer$self().overworld());
		}

		return shimmer$scheduledTaskHandler;
	}

	@Override
	public DataMap getServerData() {
		if (shimmer$serverDataMap == null) {
			shimmer$serverDataMap = new DataMap(Util.NIL_UUID, DataType.SERVER);
			shimmer$serverDataMap.load(shimmer$self(), shimmer$self().getWorldPath(LevelResource.ROOT).resolve("shimmer.nbt"));
		}

		return shimmer$serverDataMap;
	}

	@Override
	public void shimmer$postTick(PauseType paused) {
		if (shimmer$scheduledTaskHandler != null && paused.tick()) {
			shimmer$scheduledTaskHandler.tick();
		}

		ShimmerMinecraftServer.super.shimmer$postTick(paused);
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

	@Override
	public Map<ResourceLocation, ClockValue> shimmer$getClocks() {
		return shimmer$clocks;
	}
}
