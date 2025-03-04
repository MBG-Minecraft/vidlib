package dev.beast.mods.shimmer.feature.camerashake;

import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ShakeCameraPayload(CameraShake shake) implements ShimmerPacketPayload {
	public static final ShimmerPacketType<ShakeCameraPayload> TYPE = ShimmerPacketType.internal("shake_camera", CameraShake.STREAM_CODEC.map(ShakeCameraPayload::new, ShakeCameraPayload::shake));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		ctx.player().shakeCamera(shake);
	}
}
