package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

public record RemoveZonePayload(Identifier zone, int index) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<RemoveZonePayload> TYPE = VidLibPacketType.internal("zone/remove", CompositeStreamCodec.of(
		ID.STREAM_CODEC, RemoveZonePayload::zone,
		ByteBufCodecs.VAR_INT, RemoveZonePayload::index,
		RemoveZonePayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.level().getEnvironment().removeZone(zone, index);
	}
}
