package dev.latvian.mods.vidlib.feature.session;

import dev.latvian.mods.vidlib.core.VLS2CPacketConsumer;
import dev.latvian.mods.vidlib.feature.data.SyncPlayerDataPayload;
import dev.latvian.mods.vidlib.feature.input.PlayerInputChanged;
import dev.latvian.mods.vidlib.feature.input.SyncPlayerInputToClient;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Set;
import java.util.UUID;

public class ServerSessionData extends SessionData {
	private Set<String> currentTags = Set.of();

	public ServerSessionData(UUID uuid) {
		super(uuid);
	}

	public void vl$preTick(ServerPlayer player) {
		updateOverrides(player);
	}

	@Override
	public void updateOverrides(Player player) {
		super.updateOverrides(player);
		currentTags = player.getTags();
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

	public void load(ServerPlayer player) {
		dataMap.load(player.server, player.server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve("vidlib").resolve(player.getUUID() + ".nbt"));
	}

	@Override
	public Set<String> getTags(long gameTime) {
		return currentTags;
	}
}
