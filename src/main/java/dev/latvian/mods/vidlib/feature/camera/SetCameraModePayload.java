package dev.latvian.mods.vidlib.feature.camera;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;

public record SetCameraModePayload(int mode) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<SetCameraModePayload> TYPE = VidLibPacketType.internal("set_camera_mode", ByteBufCodecs.VAR_INT.map(SetCameraModePayload::new, SetCameraModePayload::mode));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().setCameraMode(mode);
	}
}
