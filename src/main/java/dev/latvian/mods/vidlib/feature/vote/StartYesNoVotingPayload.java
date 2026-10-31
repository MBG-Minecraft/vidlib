package dev.latvian.mods.vidlib.feature.vote;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;

public record StartYesNoVotingPayload(CompoundTag extraData, Component title, Component subtitle, Component yesLabel, Component noLabel, boolean closeOnVote) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<StartYesNoVotingPayload> TYPE = SimplePayloadType.vidlib("start_yes_no_voting", CompositeStreamCodec.of(
		MCStreamCodecs.COMPOUND_TAG, StartYesNoVotingPayload::extraData,
		ComponentSerialization.TRUSTED_STREAM_CODEC, StartYesNoVotingPayload::title,
		ComponentSerialization.TRUSTED_STREAM_CODEC, StartYesNoVotingPayload::subtitle,
		ComponentSerialization.TRUSTED_STREAM_CODEC, StartYesNoVotingPayload::yesLabel,
		ComponentSerialization.TRUSTED_STREAM_CODEC, StartYesNoVotingPayload::noLabel,
		ByteBufCodecs.BOOL, StartYesNoVotingPayload::closeOnVote,
		StartYesNoVotingPayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		if (ctx.isReplay()) {
			return;
		}

		ctx.player().openYesNoVotingScreen(extraData, title, subtitle, yesLabel, noLabel, closeOnVote);
	}
}
