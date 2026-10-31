package dev.latvian.mods.vidlib.feature.platform.neoforge;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.net.ConfigurationTaskHolder;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

@EventBusSubscriber(modid = VidLib.ID)
public class VidLibNetworkEventHandler {
	public static final IPayloadHandler<SimplePayload> ASYNC_HANDLER = (payload, payloadContext) -> {
		try {
			var ctx = new NeoForgePacketContext(payload, payloadContext);
			CommonGameEngine.INSTANCE.handlePacket(ctx);
		} catch (Exception ex) {
			VidLib.LOGGER.error("Failed to handle packet '%s' @ %s".formatted(payload.type().id(), payload), ex);
		}
	};

	public static final IPayloadHandler<SimplePayload> NOOP_HANDLER = (_, _) -> {
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
			register(s);
		}
	}

	private static <T extends SimplePayload> void register(AutoPacket.ScanData<T> scanData) {
		var type = scanData.type();
		var stage = scanData.stage();
		var to = scanData.to();

		VidLib.LOGGER.info("Registered " + stage.string + " " + to.string + " @AutoPacket '" + type.type().id() + "' @ " + scanData.className());

		NetworkRegistry.register(
			type.type(),
			type.streamCodec(),
			Cast.to(to == AutoPacket.To.SERVER ? ASYNC_HANDLER : NOOP_HANDLER),
			Cast.to(to == AutoPacket.To.CLIENT ? ASYNC_HANDLER : NOOP_HANDLER),
			stage.protocols,
			to.flow,
			"1",
			true
		);
	}
}
