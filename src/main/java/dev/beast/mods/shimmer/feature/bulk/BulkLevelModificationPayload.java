package dev.beast.mods.shimmer.feature.bulk;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BulkLevelModificationPayload(BulkLevelModification modification) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<BulkLevelModificationPayload> TYPE = ShimmerPacketType.internal("bulk_level_modification", BulkLevelModification.REGISTRY.valueStreamCodec().map(BulkLevelModificationPayload::new, BulkLevelModificationPayload::modification));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		ctx.player().level().bulkModify(modification);
	}
}
