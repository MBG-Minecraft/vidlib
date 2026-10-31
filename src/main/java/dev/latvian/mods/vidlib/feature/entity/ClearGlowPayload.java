package dev.latvian.mods.vidlib.feature.entity;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.network.codec.StreamCodec;

public enum ClearGlowPayload implements SimplePayload {
	INSTANCE;

	@AutoPacket
	public static final SimplePayloadType<ClearGlowPayload> TYPE = SimplePayloadType.vidlib("glow/clear", StreamCodec.unit(INSTANCE));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().vl$sessionData().setGlowColor(null, null);
	}
}
