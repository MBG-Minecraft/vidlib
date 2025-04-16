package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLMinecraftServer;
import dev.latvian.mods.vidlib.feature.clock.ClockValue;
import dev.latvian.mods.vidlib.feature.data.DataMap;
import dev.latvian.mods.vidlib.feature.data.DataType;
import dev.latvian.mods.vidlib.util.PauseType;
import dev.latvian.mods.vidlib.util.ScheduledTask;
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
public abstract class MinecraftServerMixin implements VLMinecraftServer {
	@Shadow
	@Final
	private Map<ResourceKey<Level>, ServerLevel> levels;

	@Unique
	private ScheduledTask.Handler vl$scheduledTaskHandler;

	@Unique
	private ServerLevel vl$overworld;

	@Unique
	private DataMap vl$serverDataMap;

	@Unique
	private final Map<ResourceLocation, ClockValue> vl$clocks = new HashMap<>();

	@Override
	public ScheduledTask.Handler vl$getScheduledTaskHandler() {
		if (vl$scheduledTaskHandler == null) {
			vl$scheduledTaskHandler = new ScheduledTask.Handler(vl$self(), () -> vl$self().overworld());
		}

		return vl$scheduledTaskHandler;
	}

	@Override
	public DataMap getServerData() {
		if (vl$serverDataMap == null) {
			vl$serverDataMap = new DataMap(Util.NIL_UUID, DataType.SERVER);
			vl$serverDataMap.load(vl$self(), vl$self().getWorldPath(LevelResource.ROOT).resolve("vidlib.nbt"));
		}

		return vl$serverDataMap;
	}

	@Override
	public void vl$postTick(PauseType paused) {
		if (vl$scheduledTaskHandler != null && paused.tick()) {
			vl$scheduledTaskHandler.tick();
		}

		VLMinecraftServer.super.vl$postTick(paused);
	}

	@Inject(method = "createLevels", at = @At("RETURN"))
	private void vl$createLevels(CallbackInfo ci) {
		vl$overworld = levels.get(Level.OVERWORLD);
	}

	/**
	 * @author Lat
	 * @reason Optimization
	 */
	@Overwrite
	public final ServerLevel overworld() {
		return vl$overworld;
	}

	@Override
	public Map<ResourceLocation, ClockValue> vl$getClocks() {
		return vl$clocks;
	}
}
