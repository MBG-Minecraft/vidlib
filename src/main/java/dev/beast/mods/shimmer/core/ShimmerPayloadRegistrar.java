package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public interface ShimmerPayloadRegistrar {
	static ShimmerPayloadRegistrar of(PayloadRegistrar registrar) {
		return (ShimmerPayloadRegistrar) registrar;
	}

	static ShimmerPayloadRegistrar of(RegisterPayloadHandlersEvent event) {
		return of(event.registrar("1").optional());
	}

	default <T extends ShimmerPacketPayload> void s2c(ShimmerPacketType<T> type) {
		((PayloadRegistrar) this).playToClient(type.type(), type.streamCodec(), (IPayloadHandler) ShimmerPacketType.HANDLER);
	}

	default <T extends ShimmerPacketPayload> void c2s(ShimmerPacketType<T> type) {
		((PayloadRegistrar) this).playToServer(type.type(), type.streamCodec(), (IPayloadHandler) ShimmerPacketType.HANDLER);
	}

	default <T extends ShimmerPacketPayload> void bidi(ShimmerPacketType<T> type) {
		((PayloadRegistrar) this).playBidirectional(type.type(), type.streamCodec(), (IPayloadHandler) ShimmerPacketType.HANDLER);
	}
}
