package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.feature.structure.StructureStorage;
import dev.beast.mods.shimmer.feature.zone.ZoneReloadListener;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.GAME)
public class GameEventHandler {
	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event) {
		ShimmerCommands.register(event.getDispatcher());
	}

	@SubscribeEvent
	public static void addReloadListeners(AddReloadListenerEvent event) {
		event.addListener(new ZoneReloadListener());
		event.addListener(StructureStorage.SERVER);
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

		event.getServer().refreshZones();
	}

	@SubscribeEvent
	public static void serverPostTick(ServerTickEvent.Post event) {
		event.getServer().shimmer$getScheduledTaskHandler().tick();

		var zones = event.getServer().shimmer$getZoneContainer();

		if (zones != null) {
			zones.tick(event.getServer().getLevel(zones.dimension));
		}
	}
}
