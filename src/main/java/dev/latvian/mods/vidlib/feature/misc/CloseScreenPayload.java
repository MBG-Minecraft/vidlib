package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.network.codec.StreamCodec;

public enum CloseScreenPayload implements SimplePayload {
	INSTANCE;

	@AutoPacket
	public static final SimplePayloadType<CloseScreenPayload> TYPE = SimplePayloadType.vidlib("close_screen", StreamCodec.unit(INSTANCE));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.features().closeScreen();
	}
}
