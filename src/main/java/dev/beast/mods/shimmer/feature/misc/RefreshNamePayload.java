package dev.beast.mods.shimmer.feature.misc;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import java.util.UUID;

public record RefreshNamePayload(UUID player, Component nickname) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<RefreshNamePayload> TYPE = ShimmerPacketType.internal("refresh_name", CompositeStreamCodec.of(
		ShimmerStreamCodecs.UUID, RefreshNamePayload::player,
		ComponentSerialization.TRUSTED_STREAM_CODEC, RefreshNamePayload::nickname,
		RefreshNamePayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		var p = ctx.level().getPlayerByUUID(player);

		if (p != null) {
			p.setNickname(nickname);
			p.refreshDisplayName();
			p.shimmer$sessionData().refreshListedPlayers();
		}
	}
}
