package dev.latvian.mods.vidlib.feature.entity;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;

public record SetGlowPayload(Color color, int entity) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<SetGlowPayload> TYPE = VidLibPacketType.internal("glow/add", CompositeStreamCodec.of(
		Color.STREAM_CODEC, SetGlowPayload::color,
		ByteBufCodecs.INT, SetGlowPayload::entity,
		SetGlowPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().vl$sessionData().glowColors.put(entity, color);
	}
}
