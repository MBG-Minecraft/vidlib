package dev.latvian.mods.vidlib.feature.input;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.entity.PlayerInput;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;

import java.util.UUID;

public record SyncPlayerInputToClient(UUID player, PlayerInput input) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<SyncPlayerInputToClient> TYPE = SimplePayloadType.vidlib("sync_input_to_client", CompositeStreamCodec.of(
		KLibStreamCodecs.UUID, SyncPlayerInputToClient::player,
		PlayerInput.STREAM_CODEC, SyncPlayerInputToClient::input,
		SyncPlayerInputToClient::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public boolean allowDebugLogging() {
		return false;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().vl$sessionData().updateInput(ctx.level(), player, input);
	}
}
