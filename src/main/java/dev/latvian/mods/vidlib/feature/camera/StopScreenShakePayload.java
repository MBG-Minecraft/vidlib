package dev.latvian.mods.vidlib.feature.camera;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.network.codec.StreamCodec;

public enum StopScreenShakePayload implements SimplePayload {
	INSTANCE;

	@AutoPacket
	public static final SimplePayloadType<StopScreenShakePayload> TYPE = SimplePayloadType.vidlib("stop_screen_shake", StreamCodec.unit(INSTANCE));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().stopScreenShake();
	}
}
