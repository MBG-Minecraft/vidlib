package dev.beast.mods.shimmer.feature.vote;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public record StartVotePayload(CompoundTag data, Component title, Component subtitle, Component yesLabel, Component noLabel) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<StartVotePayload> TYPE = ShimmerPacketType.internal("start_vote", CompositeStreamCodec.of(
		ShimmerStreamCodecs.COMPOUND_TAG, StartVotePayload::data,
		ComponentSerialization.TRUSTED_STREAM_CODEC, StartVotePayload::title,
		ComponentSerialization.TRUSTED_STREAM_CODEC, StartVotePayload::subtitle,
		ComponentSerialization.TRUSTED_STREAM_CODEC, StartVotePayload::yesLabel,
		ComponentSerialization.TRUSTED_STREAM_CODEC, StartVotePayload::noLabel,
		StartVotePayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		ctx.player().openVoteScreen(data, title, subtitle, yesLabel, noLabel);
	}
}
