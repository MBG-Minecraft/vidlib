package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.StreamCodec;

public enum CloseScreenPayload implements SimplePacketPayload {
	INSTANCE;

	@AutoPacket
	public static final VidLibPacketType<CloseScreenPayload> TYPE = VidLibPacketType.internal("close_screen", StreamCodec.unit(INSTANCE));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().vl$closeScreen();
	}
}
