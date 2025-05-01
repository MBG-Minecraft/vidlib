package dev.latvian.mods.vidlib.feature.camera;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

public record ShakeCameraPayload(CameraShake shake) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<ShakeCameraPayload> TYPE = VidLibPacketType.internal("shake_camera", CameraShake.STREAM_CODEC.map(ShakeCameraPayload::new, ShakeCameraPayload::shake));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().shakeCamera(shake);
	}
}
