package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.feature.zone.ZoneEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;

// @EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.GAME)
public class TestEventHandler {
	@SubscribeEvent
	public static void refreshZones(ZoneEvent.Refresh event) {
		event.set(ResourceLocation.fromNamespaceAndPath("video", "test"));
	}

	@SubscribeEvent
	public static void entityEnteredZone(ZoneEvent.EntityEntered event) {
		if (!event.getLevel().isClientSide) {
			event.getLevel().tell(Component.empty().append(event.getEntity().getName()).append(" Entered"));
		}
	}

	@SubscribeEvent
	public static void entityExitedZone(ZoneEvent.EntityExited event) {
		if (!event.getLevel().isClientSide) {
			event.getLevel().tell(Component.empty().append(event.getEntity().getName()).append(" Exited"));
		}
	}
}
