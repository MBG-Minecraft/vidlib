package dev.latvian.mods.vidlib.feature.camera;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;

public record ShakeCameraAtPositionPayload(CameraShake shake, Vec3 position, double maxDistance) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<ShakeCameraAtPositionPayload> TYPE = VidLibPacketType.internal("shake_camera_at_position", CompositeStreamCodec.of(
		CameraShake.STREAM_CODEC, ShakeCameraAtPositionPayload::shake,
		Vec3.STREAM_CODEC, ShakeCameraAtPositionPayload::position,
		ByteBufCodecs.DOUBLE, ShakeCameraAtPositionPayload::maxDistance,
		ShakeCameraAtPositionPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().shakeCamera(shake, position, maxDistance);
	}
}
