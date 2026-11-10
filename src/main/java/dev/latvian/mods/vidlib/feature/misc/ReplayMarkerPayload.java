package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.replay.api.ReplayMarkerData;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;

public record ReplayMarkerPayload(ReplayMarkerData data) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<ReplayMarkerPayload> TYPE = SimplePayloadType.vidlib("replay_marker", ReplayMarkerData.STREAM_CODEC.map(ReplayMarkerPayload::new, ReplayMarkerPayload::data));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.features().marker(data);
	}
}
