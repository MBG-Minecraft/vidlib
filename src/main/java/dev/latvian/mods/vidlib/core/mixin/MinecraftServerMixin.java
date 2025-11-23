package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.authlib.GameProfile;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.core.VLMinecraftServer;
import dev.latvian.mods.vidlib.feature.capture.PacketCapture;
import dev.latvian.mods.vidlib.feature.clock.ClockValue;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.data.DataMap;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
	private Map<UUID, GameProfile> vl$reroutedPlayers;

	@Unique
	private final Map<String, GameProfile> vl$profileByNameCache = new Object2ObjectOpenHashMap<>();

	@Unique
	private final Map<UUID, GameProfile> vl$profileByUUIDCache = new Object2ObjectOpenHashMap<>();

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
	public Map<UUID, GameProfile> vl$getReroutedPlayers() {
		if (vl$reroutedPlayers == null) {
			vl$reroutedPlayers = VLMinecraftServer.super.vl$getReroutedPlayers();
		}

		return vl$reroutedPlayers;
	}

	@Override
	public GameProfile retrieveGameProfile(UUID uuid) {
		return vl$profileByUUIDCache.computeIfAbsent(uuid, VLMinecraftServer.super::retrieveGameProfile);
	}

	@Override
	public GameProfile retrieveGameProfile(String name) {
		return vl$profileByNameCache.computeIfAbsent(name, VLMinecraftServer.super::retrieveGameProfile);
	}

	@Override
	public Collection<GameProfile> vl$getCachedGameProfiles() {
		if (vl$profileByNameCache.isEmpty() && vl$profileByUUIDCache.isEmpty()) {
			return List.of();
		}

		var map = new Object2ObjectOpenHashMap<>(vl$profileByUUIDCache);

		for (var p : vl$profileByNameCache.values()) {
			map.putIfAbsent(p.getId(), p);
		}

		return map.values();
	}

	@Override
	public void vl$clearProfileCache() {
		vl$profileByUUIDCache.clear();
		vl$profileByNameCache.clear();
	}

	@Override
	public KNumberVariables globalVariables() {
		return vl$globalVariables;
	}

	@Override
	@Nullable
	public PacketCapture vl$getPacketCapture(boolean start) {
		if (vl$packetCapture == null && start) {
			var directory = VidLibPaths.LOCAL.resolve("packet-capture");

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
