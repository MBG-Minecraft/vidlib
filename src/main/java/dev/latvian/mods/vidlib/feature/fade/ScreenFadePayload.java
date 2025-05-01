package dev.latvian.mods.vidlib.feature.fade;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

public record ScreenFadePayload(Fade fade) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<ScreenFadePayload> TYPE = VidLibPacketType.internal("screen_fade", Fade.STREAM_CODEC.map(ScreenFadePayload::new, ScreenFadePayload::fade));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.level().setScreenFade(fade);
	}
}
