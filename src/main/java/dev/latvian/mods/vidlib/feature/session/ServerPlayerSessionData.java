package dev.latvian.mods.vidlib.feature.session;

import dev.latvian.mods.klib.net.S2CPacketConsumer;
import dev.latvian.mods.klib.platform.PlatformModInfo;
import dev.latvian.mods.vidlib.feature.feature.FeatureSet;
import dev.latvian.mods.vidlib.feature.input.PlayerInputChangedEvent;
import dev.latvian.mods.vidlib.feature.input.SyncPlayerInputToClient;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

public class ServerPlayerSessionData extends PlayerSessionData {
	public static ServerPlayerSessionData of(ServerPlayer player) {
		return (ServerPlayerSessionData) PlayerSessionData.of(player);
	}

	public final MinecraftServer server;
	public final Path dataMapPath;
	public FeatureSet clientFeatureSet;
	public Map<String, PlatformModInfo> clientMods;

	public ServerPlayerSessionData(ServerSessionData worldData, MinecraftServer server, UUID uuid) {
		super(worldData, uuid);
		this.server = server;
		this.dataMapPath = VLPlatformHelper.INSTANCE.getPlayerDataDirectory(server).resolve(uuid + ".nbt");
		this.clientFeatureSet = FeatureSet.EMPTY;
		this.clientMods = Map.of();
	}

	public void preTick(ServerPlayer player) {
		CommonGameEngine.INSTANCE.tickPlayer(player, this);
	}

	public void syncPlayer(ServerPlayer player, S2CPacketConsumer packetsToEveryone) {
		if (!prevInput.equals(input)) {
			NeoForge.EVENT_BUS.post(new PlayerInputChangedEvent(player, prevInput, input));
			prevInput = input;
			packetsToEveryone.s2c(new SyncPlayerInputToClient(player.getUUID(), input));
		}
	}

	public void load() {
		dataMap.load(server, dataMapPath);
	}

	public void save() {
		dataMap.save(server, dataMapPath);
	}
}
