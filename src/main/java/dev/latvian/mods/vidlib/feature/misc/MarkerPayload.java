package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

public record MarkerPayload(MarkerData data) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<MarkerPayload> TYPE = VidLibPacketType.internal("marker", MarkerData.STREAM_CODEC.map(MarkerPayload::new, MarkerPayload::data));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.level().marker(data);
	}
}
