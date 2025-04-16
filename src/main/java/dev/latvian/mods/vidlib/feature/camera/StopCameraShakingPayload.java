package dev.latvian.mods.vidlib.feature.camera;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.StreamCodec;

public enum StopCameraShakingPayload implements SimplePacketPayload {
	INSTANCE;

	@AutoPacket
	public static final VidLibPacketType<StopCameraShakingPayload> TYPE = VidLibPacketType.internal("stop_camera_shaking", StreamCodec.unit(INSTANCE));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().stopCameraShaking();
	}
}
