package dev.latvian.mods.vidlib.feature.vote;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public record StartYesNoVotingPayload(CompoundTag extraData, Component title, Component subtitle, Component yesLabel, Component noLabel) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<StartYesNoVotingPayload> TYPE = VidLibPacketType.internal("start_yes_no_voting", CompositeStreamCodec.of(
		MCStreamCodecs.COMPOUND_TAG, StartYesNoVotingPayload::extraData,
		ComponentSerialization.TRUSTED_STREAM_CODEC, StartYesNoVotingPayload::title,
		ComponentSerialization.TRUSTED_STREAM_CODEC, StartYesNoVotingPayload::subtitle,
		ComponentSerialization.TRUSTED_STREAM_CODEC, StartYesNoVotingPayload::yesLabel,
		ComponentSerialization.TRUSTED_STREAM_CODEC, StartYesNoVotingPayload::noLabel,
		StartYesNoVotingPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().openYesNoVotingScreen(extraData, title, subtitle, yesLabel, noLabel);
	}
}
