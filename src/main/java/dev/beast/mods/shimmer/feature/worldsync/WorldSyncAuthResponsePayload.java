package dev.beast.mods.shimmer.feature.worldsync;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record WorldSyncAuthResponsePayload(String id, String displayName, String address, int port, String token) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<WorldSyncAuthResponsePayload> TYPE = ShimmerPacketType.create(WorldSync.id("auth_response"), CompositeStreamCodec.of(
		ByteBufCodecs.STRING_UTF8, WorldSyncAuthResponsePayload::id,
		ByteBufCodecs.STRING_UTF8, WorldSyncAuthResponsePayload::displayName,
		ByteBufCodecs.STRING_UTF8, WorldSyncAuthResponsePayload::address,
		ByteBufCodecs.VAR_INT, WorldSyncAuthResponsePayload::port,
		ByteBufCodecs.STRING_UTF8, WorldSyncAuthResponsePayload::token,
		WorldSyncAuthResponsePayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		ctx.player().shimmer$sessionData().worldSyncAuthResponse(this);
	}
}
