package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.replay.api.ReplayMarkerData;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

public record ReplayMarkerPayload(ReplayMarkerData data) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<ReplayMarkerPayload> TYPE = VidLibPacketType.internal("replay_marker", ReplayMarkerData.STREAM_CODEC.map(ReplayMarkerPayload::new, ReplayMarkerPayload::data));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.level().marker(data);
	}
}
