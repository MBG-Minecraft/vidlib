package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

import java.util.List;
import java.util.UUID;

public record SyncPlayerDataPayload(UUID player, List<DataMapValue> update) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<SyncPlayerDataPayload> TYPE = VidLibPacketType.internal("sync_player_data", CompositeStreamCodec.of(
		KLibStreamCodecs.UUID, SyncPlayerDataPayload::player,
		DataKey.PLAYER.valueListStreamCodec, SyncPlayerDataPayload::update,
		SyncPlayerDataPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public boolean allowDebugLogging() {
		for (var u : update) {
			if (u.key() != null && !u.key().skipLogging()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().vl$sessionData().updatePlayerData(ctx.remoteGameTime(), ctx.player(), player, update);
	}
}
