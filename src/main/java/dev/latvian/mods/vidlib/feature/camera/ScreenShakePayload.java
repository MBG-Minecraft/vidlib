package dev.latvian.mods.vidlib.feature.camera;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

public record ScreenShakePayload(ScreenShake shake) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<ScreenShakePayload> TYPE = VidLibPacketType.internal("screen_shake", ScreenShake.STREAM_CODEC.map(ScreenShakePayload::new, ScreenShakePayload::shake));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().screenShake(shake);
	}
}
