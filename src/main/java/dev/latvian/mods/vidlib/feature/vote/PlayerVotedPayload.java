package dev.latvian.mods.vidlib.feature.vote;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.common.NeoForge;

public record PlayerVotedPayload(CompoundTag extraData, int number) implements SimplePacketPayload {
	@AutoPacket(AutoPacket.To.SERVER)
	public static final VidLibPacketType<PlayerVotedPayload> TYPE = VidLibPacketType.internal("player_voted", CompositeStreamCodec.of(
		VLStreamCodecs.COMPOUND_TAG, PlayerVotedPayload::extraData,
		ByteBufCodecs.VAR_INT, PlayerVotedPayload::number,
		PlayerVotedPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		if (NeoForge.EVENT_BUS.post(new PlayerVotedEvent(ctx.player(), extraData, number)).isCanceled()) {
			ctx.player().vl$closeScreen();
		}
	}
}
