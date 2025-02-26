package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.feature.zone.ZoneReloadListener;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.GAME)
public class GameEventHandler {
	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event) {
		ShimmerCommands.register(event.getDispatcher());
	}

	@SubscribeEvent
	public static void addReloadListener(AddReloadListenerEvent event) {
		event.addListener(new ZoneReloadListener());
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			player.server.shimmer$playerJoined(player);
		}
	}

	@SubscribeEvent
	public static void serverStarted(ServerStartedEvent event) {
		if (Shimmer.defaultGameRules) {
			event.getServer().defaultGameRules();
		}

		event.getServer().shimmer$refreshZones();
	}
}
