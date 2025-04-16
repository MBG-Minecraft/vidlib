package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

import java.util.List;
import java.util.UUID;

public record SyncPlayerDataPayload(UUID player, List<DataMapValue> update) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<SyncPlayerDataPayload> TYPE = VidLibPacketType.internal("sync_player_data", CompositeStreamCodec.of(
		VLStreamCodecs.UUID, SyncPlayerDataPayload::player,
		DataType.PLAYER.valueListStreamCodec, SyncPlayerDataPayload::update,
		SyncPlayerDataPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().vl$sessionData().updateSessionData(ctx.player(), player, update);
	}
}
