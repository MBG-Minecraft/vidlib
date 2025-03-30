package dev.beast.mods.shimmer.feature.vote;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;

public record StartNumberVotingPayload(CompoundTag extraData, Component title, Component subtitle, int max, IntList unavailable) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<StartNumberVotingPayload> TYPE = ShimmerPacketType.internal("start_number_vote", CompositeStreamCodec.of(
		ShimmerStreamCodecs.COMPOUND_TAG, StartNumberVotingPayload::extraData,
		ComponentSerialization.TRUSTED_STREAM_CODEC, StartNumberVotingPayload::title,
		ComponentSerialization.TRUSTED_STREAM_CODEC, StartNumberVotingPayload::subtitle,
		ByteBufCodecs.VAR_INT, StartNumberVotingPayload::max,
		ShimmerStreamCodecs.VAR_INT_LIST, StartNumberVotingPayload::unavailable,
		StartNumberVotingPayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		ctx.player().openNumberVotingScreen(extraData, title, subtitle, max, unavailable);
	}
}
