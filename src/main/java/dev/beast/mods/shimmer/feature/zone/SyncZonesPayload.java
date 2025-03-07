package dev.beast.mods.shimmer.feature.zone;

import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record SyncZonesPayload(List<ZoneContainer> update) implements ShimmerPacketPayload {
	public static final ShimmerPacketType<SyncZonesPayload> TYPE = ShimmerPacketType.internal("sync_zones", ZoneContainer.STREAM_CODEC.list().map(SyncZonesPayload::new, SyncZonesPayload::update));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		ctx.player().shimmer$sessionData().updateZones(ctx.player().level(), update);
	}
}
