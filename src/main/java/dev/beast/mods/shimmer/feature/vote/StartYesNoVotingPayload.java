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

public record StartYesNoVotingPayload(CompoundTag extraData, Component title, Component subtitle, Component yesLabel, Component noLabel) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<StartYesNoVotingPayload> TYPE = ShimmerPacketType.internal("start_yes_no_voting", CompositeStreamCodec.of(
		ShimmerStreamCodecs.COMPOUND_TAG, StartYesNoVotingPayload::extraData,
		ComponentSerialization.TRUSTED_STREAM_CODEC, StartYesNoVotingPayload::title,
		ComponentSerialization.TRUSTED_STREAM_CODEC, StartYesNoVotingPayload::subtitle,
		ComponentSerialization.TRUSTED_STREAM_CODEC, StartYesNoVotingPayload::yesLabel,
		ComponentSerialization.TRUSTED_STREAM_CODEC, StartYesNoVotingPayload::noLabel,
		StartYesNoVotingPayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		ctx.player().openYesNoVotingScreen(extraData, title, subtitle, yesLabel, noLabel);
	}
}
