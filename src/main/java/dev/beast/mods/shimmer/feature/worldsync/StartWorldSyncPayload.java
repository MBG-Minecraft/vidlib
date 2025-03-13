package dev.beast.mods.shimmer.feature.worldsync;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public enum StartWorldSyncPayload implements ShimmerPacketPayload {
	INSTANCE;

	@AutoPacket
	public static final ShimmerPacketType<StartWorldSyncPayload> TYPE = ShimmerPacketType.create(WorldSync.id("start"), StreamCodec.unit(INSTANCE));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		ctx.player().shimmer$sessionData().startWorldSync();
	}
}
