package dev.beast.mods.shimmer.feature.net;

import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.world.level.Level;

public interface ShimmerPacketPayload {
	ShimmerPacketType<?> getType();

	default boolean allowDebugLogging() {
		return true;
	}

	default void handleAsync(ShimmerPayloadContext ctx) {
		ctx.parent().enqueueWork(() -> {
			handle(ctx);
		});
	}

	default void handle(ShimmerPayloadContext ctx) {
	}

	default ClientboundCustomPayloadPacket toS2C(Level level) {
		return new ClientboundCustomPayloadPacket(new ShimmerPacketPayloadContainer(this, level.shimmer$nextPacketId(), level.getGameTime()));
	}

	default ServerboundCustomPayloadPacket toC2S(Level level) {
		return new ServerboundCustomPayloadPacket(new ShimmerPacketPayloadContainer(this, level.shimmer$nextPacketId(), level.getGameTime()));
	}
}
