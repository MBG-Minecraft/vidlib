package dev.latvian.mods.vidlib.feature.camera;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.StreamCodec;

public enum StopScreenShakePayload implements SimplePacketPayload {
	INSTANCE;

	@AutoPacket
	public static final VidLibPacketType<StopScreenShakePayload> TYPE = VidLibPacketType.internal("stop_screen_shake", StreamCodec.unit(INSTANCE));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().stopScreenShake();
	}
}
