package dev.latvian.mods.vidlib.feature.camera;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;

public record ScreenShakePayload(ScreenShake shake) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<ScreenShakePayload> TYPE = SimplePayloadType.vidlib("screen_shake", ScreenShake.STREAM_CODEC.map(ScreenShakePayload::new, ScreenShakePayload::shake));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().screenShake(shake);
	}
}
