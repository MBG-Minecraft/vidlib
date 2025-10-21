package dev.latvian.mods.vidlib.feature.entity;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;

public record RemoveGlowPayload(int entity) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<RemoveGlowPayload> TYPE = VidLibPacketType.internal("glow/remove", CompositeStreamCodec.of(
		ByteBufCodecs.INT, RemoveGlowPayload::entity,
		RemoveGlowPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().vl$sessionData().glowColors.remove(entity);
	}
}
