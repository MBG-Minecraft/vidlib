package dev.latvian.mods.vidlib.feature.entity;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

import java.util.UUID;

public record RemoveGlowPayload(UUID entity) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<RemoveGlowPayload> TYPE = VidLibPacketType.internal("glow/remove", CompositeStreamCodec.of(
		KLibStreamCodecs.UUID, RemoveGlowPayload::entity,
		RemoveGlowPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().vl$sessionData().setGlowColor(entity, null);
	}
}
