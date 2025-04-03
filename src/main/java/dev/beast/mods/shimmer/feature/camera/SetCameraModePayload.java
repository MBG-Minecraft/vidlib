package dev.beast.mods.shimmer.feature.camera;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import net.minecraft.network.codec.ByteBufCodecs;

public record SetCameraModePayload(int mode) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<SetCameraModePayload> TYPE = ShimmerPacketType.internal("set_camera_mode", ByteBufCodecs.VAR_INT.map(SetCameraModePayload::new, SetCameraModePayload::mode));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		ctx.player().setCameraMode(mode);
	}
}
