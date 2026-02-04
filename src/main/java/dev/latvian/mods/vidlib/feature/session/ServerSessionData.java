package dev.latvian.mods.vidlib.feature.session;

import dev.latvian.mods.vidlib.core.VLS2CPacketConsumer;
import dev.latvian.mods.vidlib.feature.data.InternalPlayerData;
import dev.latvian.mods.vidlib.feature.data.SyncPlayerDataPayload;
import dev.latvian.mods.vidlib.feature.feature.FeatureSet;
import dev.latvian.mods.vidlib.feature.input.PlayerInputChanged;
import dev.latvian.mods.vidlib.feature.input.SyncPlayerInputToClient;
import dev.latvian.mods.vidlib.feature.misc.ClientModInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ServerSessionData extends SessionData {
	public final MinecraftServer server;
	private Set<String> currentTags;
	public FeatureSet clientFeatureSet;
	public Map<String, ClientModInfo> clientMods;

	public ServerSessionData(MinecraftServer server, UUID uuid) {
		super(uuid);
		this.server = server;
		this.currentTags = Set.of();
		this.clientFeatureSet = FeatureSet.EMPTY;
		this.clientMods = Map.of();
	}

	public void vl$preTick(ServerPlayer player) {
		updateOverrides(player);
	}

	@Override
	public void updateOverrides(Player player) {
		super.updateOverrides(player);
		var newTags = player.getTags();

		if (!currentTags.equals(newTags)) {
			currentTags = Set.copyOf(newTags);
			player.set(InternalPlayerData.PLAYER_TAGS, currentTags);
		}
	}

	public void vl$postTick(VLS2CPacketConsumer packetsToEveryone, ServerPlayer player) {
		if (!prevInput.equals(input)) {
			NeoForge.EVENT_BUS.post(new PlayerInputChanged(player, prevInput, input));
			prevInput = input;
			packetsToEveryone.s2c(new SyncPlayerInputToClient(player.getUUID(), input));
		}

		if (!player.level().isReplayLevel()) {
			dataMap.sync(packetsToEveryone, player, SyncPlayerDataPayload::new);
		}

		tick++;
	}

	public void load(MinecraftServer server) {
		dataMap.load(server, server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve("vidlib").resolve(uuid + ".nbt"));
	}

	@Override
	public Set<String> getTags() {
		return currentTags;
	}

	@Override
	public FeatureSet getClientFeatures() {
		return clientFeatureSet;
	}
}
