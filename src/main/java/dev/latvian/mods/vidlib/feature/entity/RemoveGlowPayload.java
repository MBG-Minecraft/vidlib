package dev.latvian.mods.vidlib.feature.entity;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;

import java.util.UUID;

public record RemoveGlowPayload(UUID entity) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<RemoveGlowPayload> TYPE = SimplePayloadType.vidlib("glow/remove", CompositeStreamCodec.of(
		KLibStreamCodecs.UUID, RemoveGlowPayload::entity,
		RemoveGlowPayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().vl$sessionData().setGlowColor(entity, null);
	}
}
