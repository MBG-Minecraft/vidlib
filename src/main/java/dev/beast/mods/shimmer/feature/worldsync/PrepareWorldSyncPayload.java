package dev.beast.mods.shimmer.feature.worldsync;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PrepareWorldSyncPayload(String ip, int port) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<PrepareWorldSyncPayload> TYPE = ShimmerPacketType.create(WorldSync.id("prepare"), CompositeStreamCodec.of(
		ByteBufCodecs.STRING_UTF8, PrepareWorldSyncPayload::ip,
		ByteBufCodecs.VAR_INT, PrepareWorldSyncPayload::port,
		PrepareWorldSyncPayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		ctx.player().shimmer$sessionData().prepareWorldSyncScreen(ip, port);
	}
}
