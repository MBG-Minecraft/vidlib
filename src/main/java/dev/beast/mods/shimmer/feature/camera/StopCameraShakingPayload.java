package dev.beast.mods.shimmer.feature.camera;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import net.minecraft.network.codec.StreamCodec;

public enum StopCameraShakingPayload implements ShimmerPacketPayload {
	INSTANCE;

	@AutoPacket
	public static final ShimmerPacketType<StopCameraShakingPayload> TYPE = ShimmerPacketType.internal("stop_camera_shaking", StreamCodec.unit(INSTANCE));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		ctx.player().stopCameraShaking();
	}
}
