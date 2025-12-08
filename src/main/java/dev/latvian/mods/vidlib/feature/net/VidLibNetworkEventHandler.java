package dev.latvian.mods.vidlib.feature.net;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.feature.ServerFeaturesConfigurationTask;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketFlow;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = VidLib.ID)
public class VidLibNetworkEventHandler {
	public static final IPayloadHandler<VidLibPacketPayloadContainer> ASYNC_HANDLER = (payload, ctx) -> {
		try {
			CommonGameEngine.INSTANCE.handlePacket(ctx, payload);
		} catch (Exception ex) {
			VidLib.LOGGER.error("Failed to handle packet '%s' #%,d @ %,d, %s".formatted(payload.type().id(), payload.uid(), payload.remoteGameTime(), payload.wrapped()), ex);
		}
	};

	@SubscribeEvent
	public static void registerConfigurationTasks(RegisterConfigurationTasksEvent event) {
		var listener = event.getListener();
		event.register(new ServerFeaturesConfigurationTask(listener));
	}

	@SubscribeEvent
	public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
		var toClient = Optional.of(PacketFlow.CLIENTBOUND);
		var toServer = Optional.of(PacketFlow.SERVERBOUND);
		var common = List.of(ConnectionProtocol.PLAY, ConnectionProtocol.CONFIGURATION);
		var config = List.of(ConnectionProtocol.CONFIGURATION);
		var play = List.of(ConnectionProtocol.PLAY);

		for (var s : AutoPacket.SCANNED.get()) {
			List<ConnectionProtocol> protocols;
			String protocolsString;

			if (s.stage().contains(AutoPacket.Stage.CONFIG) && s.stage().contains(AutoPacket.Stage.GAME)) {
				protocols = common;
				protocolsString = "common";
			} else if (s.stage().contains(AutoPacket.Stage.CONFIG)) {
				protocols = config;
				protocolsString = "config";
			} else if (s.stage().contains(AutoPacket.Stage.GAME)) {
				protocols = play;
				protocolsString = "game";
			} else {
				throw new IllegalStateException("'stage' field of AutoPacket can't be empty");
			}

			Optional<PacketFlow> packetFlow;
			String packetFlowString;

			if (s.to().contains(AutoPacket.To.CLIENT) && s.to().contains(AutoPacket.To.SERVER)) {
				packetFlow = Optional.empty();
				packetFlowString = "BIDI";
			} else if (s.to().contains(AutoPacket.To.CLIENT)) {
				packetFlow = toClient;
				packetFlowString = "S2C";
			} else if (s.to().contains(AutoPacket.To.SERVER)) {
				packetFlow = toServer;
				packetFlowString = "C2S";
			} else {
				throw new IllegalStateException("'to' field of AutoPacket can't be empty");
			}

			VidLib.LOGGER.info("Registered " + protocolsString + " " + packetFlowString + " @AutoPacket '" + s.type().type().id() + "' @ " + s.className());

			NetworkRegistry.register(
				s.type().type(),
				s.type().streamCodec(),
				ASYNC_HANDLER,
				List.copyOf(protocols),
				packetFlow,
				"1",
				true
			);
		}
	}
}
