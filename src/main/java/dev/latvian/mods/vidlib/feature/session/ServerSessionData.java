package dev.latvian.mods.vidlib.feature.session;

import dev.latvian.mods.vidlib.core.VLS2CPacketConsumer;
import dev.latvian.mods.vidlib.feature.data.InternalPlayerData;
import dev.latvian.mods.vidlib.feature.feature.FeatureSet;
import dev.latvian.mods.vidlib.feature.input.PlayerInputChanged;
import dev.latvian.mods.vidlib.feature.input.SyncPlayerInputToClient;
import dev.latvian.mods.vidlib.feature.misc.PlatformModInfo;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ServerSessionData extends SessionData {
	public final MinecraftServer server;
	public final Path dataMapPath;
	private Set<String> currentTags;
	public FeatureSet clientFeatureSet;
	public Map<String, PlatformModInfo> clientMods;

	public ServerSessionData(MinecraftServer server, UUID uuid) {
		super(uuid, server);
		this.server = server;
		this.dataMapPath = server.vl$getPlayerDataDirectory().resolve(uuid + ".nbt");
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

		if (!CommonGameEngine.INSTANCE.hasImprovedPlayerTags()) {
			var newTags = player.getTags();

			if (!currentTags.equals(newTags)) {
				currentTags = Set.copyOf(newTags);
				player.set(InternalPlayerData.PLAYER_TAGS, currentTags);
			}
		}
	}

	public void syncPlayer(ServerPlayer player, VLS2CPacketConsumer packetsToEveryone) {
		if (!prevInput.equals(input)) {
			NeoForge.EVENT_BUS.post(new PlayerInputChanged(player, prevInput, input));
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

	@Override
	public Set<String> getTags() {
		return CommonGameEngine.INSTANCE.hasImprovedPlayerTags() ? dataMap.get(InternalPlayerData.PLAYER_TAGS) : currentTags;
	}

	@Override
	public void setTags(Set<String> tags) {
		if (CommonGameEngine.INSTANCE.hasImprovedPlayerTags()) {
			super.setTags(tags);
		} else {
			throw new IllegalStateException("hasImprovedPlayerTags() must be true to use setTags");
		}
	}

	@Override
	public FeatureSet getClientFeatures() {
		return clientFeatureSet;
	}
}
