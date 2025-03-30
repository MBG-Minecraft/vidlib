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

public record PlayerVotedPayload(CompoundTag extraData, int number) implements ShimmerPacketPayload {
	@AutoPacket(AutoPacket.To.SERVER)
	public static final ShimmerPacketType<PlayerVotedPayload> TYPE = ShimmerPacketType.internal("player_voted", CompositeStreamCodec.of(
		ShimmerStreamCodecs.COMPOUND_TAG, PlayerVotedPayload::extraData,
		ByteBufCodecs.VAR_INT, PlayerVotedPayload::number,
		PlayerVotedPayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		if (NeoForge.EVENT_BUS.post(new PlayerVotedEvent(ctx.player(), extraData, number)).isCanceled()) {
			ctx.player().endVote();
		}
	}
}
