package dev.beast.mods.shimmer.feature.camerashake;

import dev.beast.mods.shimmer.ShimmerNet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class StopCameraShakingPayload implements CustomPacketPayload {
	public static final Type<StopCameraShakingPayload> TYPE = ShimmerNet.type("stop_camera_shaking");
	public static final StopCameraShakingPayload INSTANCE = new StopCameraShakingPayload();
	public static final StreamCodec<RegistryFriendlyByteBuf, StopCameraShakingPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public void handle(IPayloadContext ctx) {
		ctx.enqueueWork(() -> ctx.player().stopCameraShaking());
	}
}
