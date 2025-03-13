package dev.beast.mods.shimmer.feature.camerashake;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public enum StopCameraShakingPayload implements ShimmerPacketPayload {
	INSTANCE;

	@AutoPacket
	public static final ShimmerPacketType<StopCameraShakingPayload> TYPE = ShimmerPacketType.internal("stop_camera_shaking", StreamCodec.unit(INSTANCE));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		ctx.player().stopCameraShaking();
	}
}
