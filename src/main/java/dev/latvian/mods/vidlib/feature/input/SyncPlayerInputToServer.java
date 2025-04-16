package dev.latvian.mods.vidlib.feature.input;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

public record SyncPlayerInputToServer(PlayerInput input) implements SimplePacketPayload {
	@AutoPacket(AutoPacket.To.SERVER)
	public static final VidLibPacketType<SyncPlayerInputToServer> TYPE = VidLibPacketType.internal("sync_input_to_server", PlayerInput.STREAM_CODEC.map(SyncPlayerInputToServer::new, SyncPlayerInputToServer::input));

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
		ctx.player().vl$sessionData().input = input;
	}
}
