package dev.beast.mods.shimmer.feature.camera;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;

public record ShakeCameraPayload(CameraShake shake) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<ShakeCameraPayload> TYPE = ShimmerPacketType.internal("shake_camera", CameraShake.STREAM_CODEC.map(ShakeCameraPayload::new, ShakeCameraPayload::shake));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		if (!ctx.player().isReplayCamera()) {
			ctx.player().shakeCamera(shake);
		}
	}
}
