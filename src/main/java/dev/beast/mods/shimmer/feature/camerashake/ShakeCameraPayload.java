package dev.beast.mods.shimmer.feature.camerashake;

import dev.beast.mods.shimmer.ShimmerNet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ShakeCameraPayload(CameraShake shake) implements CustomPacketPayload {
	public static final Type<ShakeCameraPayload> TYPE = ShimmerNet.type("shake_camera");
	public static final StreamCodec<RegistryFriendlyByteBuf, ShakeCameraPayload> STREAM_CODEC = CameraShake.STREAM_CODEC.map(ShakeCameraPayload::new, ShakeCameraPayload::shake);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public void handle(IPayloadContext ctx) {
		ctx.enqueueWork(() -> ctx.player().shakeCamera(shake));
	}
}
