package dev.beast.mods.shimmer.feature.camerashake;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;

public record ShakeCameraAtPositionPayload(CameraShake shake, Vec3 position, double maxDistance) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<ShakeCameraAtPositionPayload> TYPE = ShimmerPacketType.internal("shake_camera_at_position", CompositeStreamCodec.of(
		CameraShake.STREAM_CODEC, ShakeCameraAtPositionPayload::shake,
		Vec3.STREAM_CODEC, ShakeCameraAtPositionPayload::position,
		ByteBufCodecs.DOUBLE, ShakeCameraAtPositionPayload::maxDistance,
		ShakeCameraAtPositionPayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		ctx.player().shakeCamera(shake, position, maxDistance);
	}
}
