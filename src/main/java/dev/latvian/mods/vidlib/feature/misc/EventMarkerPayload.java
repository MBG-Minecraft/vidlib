package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

public record EventMarkerPayload(EventMarkerData data) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<EventMarkerPayload> TYPE = VidLibPacketType.internal("event_marker", EventMarkerData.STREAM_CODEC.map(EventMarkerPayload::new, EventMarkerPayload::data));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.level().marker(data);
	}
}
