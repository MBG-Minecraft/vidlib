package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.feature.cutscene.CutsceneReloadListener;
import dev.beast.mods.shimmer.feature.structure.StructureStorage;
import dev.beast.mods.shimmer.feature.zone.ActiveZones;
import dev.beast.mods.shimmer.feature.zone.UpdateZonesPayload;
import dev.beast.mods.shimmer.feature.zone.ZoneReloadListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.GAME)
public class GameEventHandler {
	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event) {
		ShimmerCommands.register(event.getDispatcher(), event.getBuildContext());
	}

	@SubscribeEvent
	public static void addReloadListeners(AddReloadListenerEvent event) {
		event.addListener(new ZoneReloadListener());
		event.addListener(StructureStorage.SERVER);
		event.addListener(new CutsceneReloadListener());
	}

	@SubscribeEvent
	public static void syncReload(OnDatapackSyncEvent event) {
		var packet = new ClientboundCustomPayloadPacket(new UpdateZonesPayload(List.copyOf(ActiveZones.SERVER.containers.values())));
		event.getRelevantPlayers().forEach(player -> player.connection.send(packet));
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
	}

	@SubscribeEvent
	public static void serverPostTick(ServerTickEvent.Post event) {
		event.getServer().shimmer$postTick();
	}
}
