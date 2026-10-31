package dev.latvian.mods.vidlib.feature.entity;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;

import java.util.UUID;

public record SetGlowPayload(UUID entity, Color color) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<SetGlowPayload> TYPE = SimplePayloadType.vidlib("glow/set", CompositeStreamCodec.of(
		KLibStreamCodecs.UUID, SetGlowPayload::entity,
		Color.STREAM_CODEC, SetGlowPayload::color,
		SetGlowPayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().vl$sessionData().setGlowColor(entity, color);
	}
}
