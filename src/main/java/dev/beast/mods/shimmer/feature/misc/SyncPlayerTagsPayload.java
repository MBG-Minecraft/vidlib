package dev.beast.mods.shimmer.feature.misc;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.List;
import java.util.UUID;

public record SyncPlayerTagsPayload(UUID player, List<String> tags) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<SyncPlayerTagsPayload> TYPE = ShimmerPacketType.internal("sync_player_tags", CompositeStreamCodec.of(
		ShimmerStreamCodecs.UUID, SyncPlayerTagsPayload::player,
		ByteBufCodecs.STRING_UTF8.list(), SyncPlayerTagsPayload::tags,
		SyncPlayerTagsPayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		ctx.player().shimmer$sessionData().updatePlayerTags(ctx.remoteGameTime(), ctx.player(), player, tags);
	}
}
