package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;

public record UpdateZonePayload(ResourceLocation zone, int index, Zone zoneData) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<UpdateZonePayload> TYPE = VidLibPacketType.internal("zone/update", CompositeStreamCodec.of(
		ID.STREAM_CODEC, UpdateZonePayload::zone,
		ByteBufCodecs.VAR_INT, UpdateZonePayload::index,
		Zone.STREAM_CODEC, UpdateZonePayload::zoneData,
		UpdateZonePayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.level().getEnvironment().updateZone(zone, index, zoneData);
	}
}
