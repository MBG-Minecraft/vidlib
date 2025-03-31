package dev.beast.mods.shimmer.feature.fade;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;

public record ScreenFadePayload(Fade fade) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<ScreenFadePayload> TYPE = ShimmerPacketType.internal("screen_fade", Fade.STREAM_CODEC.map(ScreenFadePayload::new, ScreenFadePayload::fade));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		ctx.level().setScreenFade(fade);
	}
}
