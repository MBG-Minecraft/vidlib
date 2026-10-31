package dev.latvian.mods.vidlib.feature.input;

import dev.latvian.mods.klib.entity.PlayerInput;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;

public record SyncPlayerInputToServer(PlayerInput input) implements SimplePayload {
	@AutoPacket(to = AutoPacket.To.SERVER)
	public static final SimplePayloadType<SyncPlayerInputToServer> TYPE = SimplePayloadType.vidlib("sync_input_to_server", PlayerInput.STREAM_CODEC.map(SyncPlayerInputToServer::new, SyncPlayerInputToServer::input));

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
		ctx.player().vl$sessionData().input = input;

		var v = ctx.player().getVehicle();

		if (v != null) {
			v.setPilotInput(ctx.player(), input);
		}
	}
}
