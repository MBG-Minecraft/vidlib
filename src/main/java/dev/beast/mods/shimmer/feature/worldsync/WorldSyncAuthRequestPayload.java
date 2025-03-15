package dev.beast.mods.shimmer.feature.worldsync;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import net.minecraft.network.codec.StreamCodec;

public enum WorldSyncAuthRequestPayload implements ShimmerPacketPayload {
	INSTANCE;

	@AutoPacket(AutoPacket.To.SERVER)
	public static final ShimmerPacketType<WorldSyncAuthRequestPayload> TYPE = ShimmerPacketType.create(WorldSync.id("auth_request"), StreamCodec.unit(INSTANCE));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}
}
