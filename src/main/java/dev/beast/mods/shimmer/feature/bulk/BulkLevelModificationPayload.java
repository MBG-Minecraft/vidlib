package dev.beast.mods.shimmer.feature.bulk;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BulkLevelModificationPayload(BulkLevelModification modification, long gameTime) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<BulkLevelModificationPayload> TYPE = ShimmerPacketType.internal("bulk_level_modification", CompositeStreamCodec.of(
		BulkLevelModification.REGISTRY.valueStreamCodec(), BulkLevelModificationPayload::modification,
		ByteBufCodecs.VAR_LONG, BulkLevelModificationPayload::gameTime,
		BulkLevelModificationPayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		ctx.player().level().bulkModify(modification);
	}
}
