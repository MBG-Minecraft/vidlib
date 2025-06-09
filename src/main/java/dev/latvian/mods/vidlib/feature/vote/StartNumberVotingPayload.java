package dev.latvian.mods.vidlib.feature.vote;

import dev.latvian.mods.klib.codec.CollectionStreamCodecs;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;

public record StartNumberVotingPayload(CompoundTag extraData, Component title, Component subtitle, int max, IntList unavailable) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<StartNumberVotingPayload> TYPE = VidLibPacketType.internal("start_number_vote", CompositeStreamCodec.of(
		MCStreamCodecs.COMPOUND_TAG, StartNumberVotingPayload::extraData,
		ComponentSerialization.TRUSTED_STREAM_CODEC, StartNumberVotingPayload::title,
		ComponentSerialization.TRUSTED_STREAM_CODEC, StartNumberVotingPayload::subtitle,
		ByteBufCodecs.VAR_INT, StartNumberVotingPayload::max,
		CollectionStreamCodecs.VAR_INT_LIST, StartNumberVotingPayload::unavailable,
		StartNumberVotingPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().openNumberVotingScreen(extraData, title, subtitle, max, unavailable);
	}
}
