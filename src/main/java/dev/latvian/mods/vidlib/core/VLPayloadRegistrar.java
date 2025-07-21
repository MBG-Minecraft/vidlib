package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.VidLibConfig;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketPayloadContainer;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public interface VLPayloadRegistrar {
	IPayloadHandler<VidLibPacketPayloadContainer> HANDLER = (payload, ctx) -> {
		try {
			if (payload.wrapped().allowDebugLogging()) {
				if (VidLibConfig.debugS2CPackets && !(ctx.player() instanceof ServerPlayer)) {
					VidLib.LOGGER.info("S2C Packet '%s' #%,d @ %,d: %s".formatted(payload.type().id(), payload.uid(), payload.remoteGameTime(), payload.wrapped()));
				}
			}

			payload.wrapped().handleAsync(new Context(ctx, payload.type().id(), payload.uid(), payload.remoteGameTime()));
		} catch (Exception ex) {
			VidLib.LOGGER.error("Failed to handle packet '%s' #%,d @ %,d, %s".formatted(payload.type().id(), payload.uid(), payload.remoteGameTime(), payload.wrapped()), ex);
		}
	};

	static VLPayloadRegistrar of(PayloadRegistrar registrar) {
		return (VLPayloadRegistrar) registrar;
	}

	static VLPayloadRegistrar of(RegisterPayloadHandlersEvent event) {
		return of(event.registrar("1").optional());
	}

	default <T extends SimplePacketPayload> void s2c(VidLibPacketType<T> type) {
		((PayloadRegistrar) this).playToClient(type.type(), type.streamCodec(), HANDLER);
	}

	default <T extends SimplePacketPayload> void c2s(VidLibPacketType<T> type) {
		((PayloadRegistrar) this).playToServer(type.type(), type.streamCodec(), HANDLER);
	}

	default <T extends SimplePacketPayload> void bidi(VidLibPacketType<T> type) {
		((PayloadRegistrar) this).playBidirectional(type.type(), type.streamCodec(), HANDLER);
	}
}
