package dev.latvian.mods.vidlib.feature.camera;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.network.codec.ByteBufCodecs;

public record SetCameraModePayload(int mode) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<SetCameraModePayload> TYPE = SimplePayloadType.vidlib("set_camera_mode", ByteBufCodecs.VAR_INT.map(SetCameraModePayload::new, SetCameraModePayload::mode));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().setCameraMode(mode);
	}
}
