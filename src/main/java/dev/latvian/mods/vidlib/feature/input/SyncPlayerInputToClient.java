package dev.latvian.mods.vidlib.feature.input;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

import java.util.UUID;

public record SyncPlayerInputToClient(UUID player, PlayerInput input) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<SyncPlayerInputToClient> TYPE = VidLibPacketType.internal("sync_input_to_client", CompositeStreamCodec.of(
		VLStreamCodecs.UUID, SyncPlayerInputToClient::player,
		PlayerInput.STREAM_CODEC, SyncPlayerInputToClient::input,
		SyncPlayerInputToClient::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public boolean allowDebugLogging() {
		return false;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().vl$sessionData().updateInput(player, input);
	}
}
