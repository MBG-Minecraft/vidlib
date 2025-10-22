package dev.latvian.mods.vidlib.feature.entity;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

import java.util.UUID;

public record SetGlowPayload(UUID entity, Color color) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<SetGlowPayload> TYPE = VidLibPacketType.internal("glow/set", CompositeStreamCodec.of(
		KLibStreamCodecs.UUID, SetGlowPayload::entity,
		Color.STREAM_CODEC, SetGlowPayload::color,
		SetGlowPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().vl$sessionData().setGlowColor(entity, color);
	}
}
