package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.feature.zone.ZoneEvent;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.GAME)
public class TestEventHandler {
	@SubscribeEvent
	public static void entityEnteredZone(ZoneEvent.EntityEntered event) {
		if (!event.getLevel().isClientSide) {
			event.getLevel().status(Component.empty().append(event.getEntity().getName()).append(" Entered"));
		}
	}

	@SubscribeEvent
	public static void entityExitedZone(ZoneEvent.EntityExited event) {
		if (!event.getLevel().isClientSide) {
			event.getLevel().status(Component.empty().append(event.getEntity().getName()).append(" Exited"));
		}
	}
}
