package dev.latvian.mods.vidlib.feature.screeneffect.fade;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;

public record ScreenFadePayload(Fade fade) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<ScreenFadePayload> TYPE = SimplePayloadType.vidlib("screen_fade", Fade.STREAM_CODEC.map(ScreenFadePayload::new, ScreenFadePayload::fade));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.features().setScreenFade(fade);
	}
}
