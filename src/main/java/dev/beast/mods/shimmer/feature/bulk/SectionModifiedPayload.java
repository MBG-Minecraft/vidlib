package dev.beast.mods.shimmer.feature.bulk;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import net.minecraft.core.SectionPos;
import net.minecraft.network.codec.ByteBufCodecs;

public record SectionModifiedPayload(boolean undoable, SectionPos section, BulkLevelModification modification) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<SectionModifiedPayload> TYPE = ShimmerPacketType.internal("section_modified", CompositeStreamCodec.of(
		ByteBufCodecs.BOOL, SectionModifiedPayload::undoable,
		ShimmerStreamCodecs.SECTION_POS, SectionModifiedPayload::section,
		BulkLevelModification.REGISTRY.valueStreamCodec(), SectionModifiedPayload::modification,
		SectionModifiedPayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		ctx.level().bulkModify(undoable, modification);
	}
}
