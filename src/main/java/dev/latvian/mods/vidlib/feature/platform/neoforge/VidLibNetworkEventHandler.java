package dev.latvian.mods.vidlib.feature.platform.neoforge;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.net.ConfigurationTaskHolder;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketPayloadContainer;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

@EventBusSubscriber(modid = VidLib.ID)
public class VidLibNetworkEventHandler {
	public static final IPayloadHandler<VidLibPacketPayloadContainer> ASYNC_HANDLER = (payload, payloadContext) -> {
		try {
			var ctx = new NeoForgePacketContext(payload, payloadContext);
			CommonGameEngine.INSTANCE.handlePacket(ctx);
		} catch (Exception ex) {
			VidLib.LOGGER.error("Failed to handle packet '%s' #%,d @ %,d, %s".formatted(payload.type().id(), payload.uid(), payload.remoteGameTime(), payload.wrapped()), ex);
		}
	};

	@SubscribeEvent
	public static void registerConfigurationTasks(RegisterConfigurationTasksEvent event) {
		var listener = event.getListener();
		var channelInfo = new NeoForgeNetworkChannelInfo(listener);

		for (var s : AutoRegister.SCANNED.get()) {
			if (s.value() instanceof ConfigurationTaskHolder holder) {
				holder.register(listener, channelInfo, event::register);
			}
		}
	}

	@SubscribeEvent
	public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
		for (var s : AutoPacket.SCANNED.get()) {
			VidLib.LOGGER.info("Registered " + s.stage().string + " " + s.to().string + " @AutoPacket '" + s.type().type().id() + "' @ " + s.className());

			NetworkRegistry.register(
				s.type().type(),
				s.type().streamCodec(),
				ASYNC_HANDLER,
				s.stage().protocols,
				s.to().flow,
				"1",
				true
			);
		}
	}
}
