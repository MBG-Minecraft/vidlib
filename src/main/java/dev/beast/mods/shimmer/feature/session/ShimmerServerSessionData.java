package dev.beast.mods.shimmer.feature.session;

import dev.beast.mods.shimmer.core.ShimmerS2CPacketConsumer;
import dev.beast.mods.shimmer.feature.data.SyncPlayerDataPayload;
import dev.beast.mods.shimmer.feature.input.PlayerInputChanged;
import dev.beast.mods.shimmer.feature.input.SyncPlayerInputToClient;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.neoforged.neoforge.common.NeoForge;

public class ShimmerServerSessionData extends ShimmerSessionData {
	public final ServerGamePacketListenerImpl connection;

	public ShimmerServerSessionData(ServerGamePacketListenerImpl connection) {
		super(connection.player.getUUID());
		this.connection = connection;
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
