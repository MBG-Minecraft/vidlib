package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;

public record UpdateZonePayload(Ref<ZoneContainer> zone, int index, ZoneVolume zoneVolume) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<UpdateZonePayload> TYPE = VidLibPacketType.internal("zone/update", CompositeStreamCodec.of(
		ZoneContainer.STREAM_CODEC, UpdateZonePayload::zone,
		ByteBufCodecs.VAR_INT, UpdateZonePayload::index,
		ZoneVolume.STREAM_CODEC, UpdateZonePayload::zoneVolume,
		UpdateZonePayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.level().getEnvironment().updateZone(zone, index, zoneVolume);
	}
}
