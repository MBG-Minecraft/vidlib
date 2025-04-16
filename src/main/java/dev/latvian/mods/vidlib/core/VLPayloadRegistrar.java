package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public interface VLPayloadRegistrar {
	static VLPayloadRegistrar of(PayloadRegistrar registrar) {
		return (VLPayloadRegistrar) registrar;
	}

	static VLPayloadRegistrar of(RegisterPayloadHandlersEvent event) {
		return of(event.registrar("1").optional());
	}

	default <T extends SimplePacketPayload> void s2c(VidLibPacketType<T> type) {
		((PayloadRegistrar) this).playToClient(type.type(), type.streamCodec(), VidLibPacketType.HANDLER);
	}

	default <T extends SimplePacketPayload> void c2s(VidLibPacketType<T> type) {
		((PayloadRegistrar) this).playToServer(type.type(), type.streamCodec(), VidLibPacketType.HANDLER);
	}

	default <T extends SimplePacketPayload> void bidi(VidLibPacketType<T> type) {
		((PayloadRegistrar) this).playBidirectional(type.type(), type.streamCodec(), VidLibPacketType.HANDLER);
	}
}
