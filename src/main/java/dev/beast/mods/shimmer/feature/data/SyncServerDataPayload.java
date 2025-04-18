package dev.beast.mods.shimmer.feature.data;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;

import java.util.List;

public record SyncServerDataPayload(List<DataMapValue> update) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<SyncServerDataPayload> TYPE = ShimmerPacketType.internal("sync_server_data", DataType.SERVER.valueListStreamCodec.map(SyncServerDataPayload::new, SyncServerDataPayload::update));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public boolean allowDebugLogging() {
		for (var u : update) {
			if (!u.type().skipLogging()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		ctx.player().shimmer$sessionData().updateServerData(ctx.remoteGameTime(), ctx.player(), update);
	}
}
