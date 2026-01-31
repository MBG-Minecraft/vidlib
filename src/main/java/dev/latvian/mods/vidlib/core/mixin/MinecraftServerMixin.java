package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.core.VLMinecraftServer;
import dev.latvian.mods.vidlib.feature.capture.PacketCapture;
import dev.latvian.mods.vidlib.feature.clock.ClockValue;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.data.DataMap;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import dev.latvian.mods.vidlib.math.knumber.KNumberVariables;
import dev.latvian.mods.vidlib.util.PauseType;
import dev.latvian.mods.vidlib.util.ScheduledTask;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Files;
import java.util.Map;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements VLMinecraftServer {
	@Shadow
	@Final
	private Map<ResourceKey<Level>, ServerLevel> levels;

	@Unique
	private final RandomSource vl$sessionRandom = RandomSource.create();

	@Unique
	private int vl$sessionId = 0;

	@Unique
	private ScheduledTask.Handler vl$scheduledTaskHandler;

	@Unique
	private ServerLevel vl$overworld;

	@Unique
	private DataMap vl$serverDataMap;

	@Unique
	private final Map<ResourceLocation, ClockValue> vl$clocks = new Object2ObjectOpenHashMap<>();

	@Unique
	private final KNumberVariables vl$globalVariables = new KNumberVariables();

	@Unique
	private PacketCapture vl$packetCapture;

	@Override
	public RandomSource vl$sessionRandom() {
		return vl$sessionRandom;
	}

	@Override
	public int vl$sessionId() {
		while (vl$sessionId == 0) {
			vl$sessionId = vl$sessionRandom.nextInt();
		}

		return vl$sessionId;
	}

	@Override
	public ScheduledTask.Handler vl$getScheduledTaskHandler() {
		if (vl$scheduledTaskHandler == null) {
			vl$scheduledTaskHandler = new ScheduledTask.Handler(this::getGameTime);
		}

		return vl$scheduledTaskHandler;
	}

	@Override
	public DataMap getServerData() {
		if (vl$serverDataMap == null) {
			vl$serverDataMap = new DataMap(Util.NIL_UUID, DataKey.SERVER);
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

	@Override
	public KNumberVariables globalVariables() {
		return vl$globalVariables;
	}

	@Override
	@Nullable
	public PacketCapture vl$getPacketCapture(boolean start) {
		if (vl$packetCapture == null && start && CommonGameEngine.INSTANCE.disablePacketCapture()) {
			var directory = VidLibPaths.LOCAL.get().resolve("packet-capture");

			if (Files.notExists(directory)) {
				try {
					Files.createDirectories(directory);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			vl$packetCapture = new PacketCapture(vl$self(), vl$sessionId(), directory);
		}

		return vl$packetCapture;
	}
}
