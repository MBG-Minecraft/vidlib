package dev.beast.mods.shimmer.feature.session;

import dev.beast.mods.shimmer.core.ShimmerS2CPacketConsumer;
import dev.beast.mods.shimmer.feature.data.SyncPlayerDataPayload;
import dev.beast.mods.shimmer.feature.input.PlayerInputChanged;
import dev.beast.mods.shimmer.feature.input.SyncPlayerInputToClient;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;

import java.util.UUID;

public class ShimmerServerSessionData extends ShimmerSessionData {
	public ShimmerServerSessionData(UUID uuid) {
		super(uuid);
	}

	public void shimmer$preTick(ServerPlayer player) {
		updateOverrides(player);
	}

	public void shimmer$postTick(ShimmerS2CPacketConsumer packetsToEveryone, ServerPlayer player) {
		if (!prevInput.equals(input)) {
			NeoForge.EVENT_BUS.post(new PlayerInputChanged(player, prevInput, input));
			prevInput = input;
			packetsToEveryone.s2c(new SyncPlayerInputToClient(player.getUUID(), input));
		}

		dataMap.sync(packetsToEveryone, player, SyncPlayerDataPayload::new);
	}
}
