package dev.beast.mods.shimmer.feature.data;

import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record SyncServerDataPayload(List<DataMapValue> serverData) implements ShimmerPacketPayload {
	public static final ShimmerPacketType<SyncServerDataPayload> TYPE = ShimmerPacketType.internal("sync_server_data", DataType.SERVER.valueListStreamCodec.map(SyncServerDataPayload::new, SyncServerDataPayload::serverData));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		ctx.player().shimmer$sessionData().updateServerData(serverData);
	}
}
