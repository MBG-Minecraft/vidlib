package dev.beast.mods.shimmer.feature.vote;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.common.NeoForge;

public record VotePayload(CompoundTag data, boolean yes) implements ShimmerPacketPayload {
	@AutoPacket(AutoPacket.To.SERVER)
	public static final ShimmerPacketType<VotePayload> TYPE = ShimmerPacketType.internal("vote", CompositeStreamCodec.of(
		ShimmerStreamCodecs.COMPOUND_TAG, VotePayload::data,
		ByteBufCodecs.BOOL, VotePayload::yes,
		VotePayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		if (NeoForge.EVENT_BUS.post(new VoteEvent(ctx.player(), data, yes)).isCanceled()) {
			ctx.player().endVote();
		}
	}
}
