package dev.beast.mods.shimmer.feature.net;

import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface ShimmerPacketPayload extends CustomPacketPayload {
	ShimmerPacketType<?> getType();

	@Override
	default Type<? extends CustomPacketPayload> type() {
		return getType().type();
	}

	default void handleAsync(IPayloadContext ctx) {
		ctx.enqueueWork(() -> handle(ctx));
	}

	default void handle(IPayloadContext ctx) {
	}

	default ClientboundCustomPayloadPacket toS2C() {
		return new ClientboundCustomPayloadPacket(this);
	}

	default ServerboundCustomPayloadPacket toC2S() {
		return new ServerboundCustomPayloadPacket(this);
	}
}
