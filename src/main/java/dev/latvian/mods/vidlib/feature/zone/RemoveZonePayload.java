package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

import java.util.UUID;

public record RemoveZonePayload(UUID uuid) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<RemoveZonePayload> TYPE = VidLibPacketType.internal("remove_zone", VLStreamCodecs.UUID.map(RemoveZonePayload::new, RemoveZonePayload::uuid));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.level().getEnvironment().removeZone(uuid);
	}
}
