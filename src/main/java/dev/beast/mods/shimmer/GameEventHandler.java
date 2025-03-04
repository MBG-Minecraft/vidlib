package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.feature.clock.Clock;
import dev.beast.mods.shimmer.feature.clock.ClockFont;
import dev.beast.mods.shimmer.feature.clock.SyncClockFontsPayload;
import dev.beast.mods.shimmer.feature.clock.SyncClocksPayload;
import dev.beast.mods.shimmer.feature.cutscene.Cutscene;
import dev.beast.mods.shimmer.feature.entity.EntityOverride;
import dev.beast.mods.shimmer.feature.structure.StructureStorage;
import dev.beast.mods.shimmer.feature.toolitem.ToolItem;
import dev.beast.mods.shimmer.feature.zone.SyncZonesPayload;
import dev.beast.mods.shimmer.feature.zone.ZoneLoader;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.GAME)
public class GameEventHandler {
	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event) {
		ShimmerCommands.register(event.getDispatcher(), event.getBuildContext());
	}

	@SubscribeEvent
	public static void addReloadListeners(AddReloadListenerEvent event) {
		event.addListener(new ZoneLoader());
		event.addListener(StructureStorage.SERVER);
		event.addListener(new Cutscene.Loader());
		event.addListener(new ClockFont.Loader());
		event.addListener(new Clock.Loader());
	}

	@SubscribeEvent
	public static void syncReload(OnDatapackSyncEvent event) {
		var list = new ArrayList<Packet<? super ClientGamePacketListener>>();

		list.add(new SyncZonesPayload(List.copyOf(ZoneLoader.ALL.containers.values())).toS2C());
		list.add(new SyncClockFontsPayload(List.copyOf(ClockFont.SERVER.values())).toS2C());
		list.add(new SyncClocksPayload(List.copyOf(Clock.SERVER_INSTANCES.values())).toS2C());

		var packet = new ClientboundBundlePacket(list);

		for (var player : event.getRelevantPlayers().toList()) {
			player.s2c(packet);
		}
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
	public static void serverPreTick(ServerTickEvent.Pre event) {
		event.getServer().shimmer$preTick();
	}

	@SubscribeEvent
	public static void serverPostTick(ServerTickEvent.Post event) {
		event.getServer().shimmer$postTick();
	}

	@SubscribeEvent
	public static void useItemOnBlock(UseItemOnBlockEvent event) {
		if (event.getPlayer() != null) {
			var tool = ToolItem.of(event.getItemStack());

			if (tool != null && tool.useOnBlock(event.getPlayer(), event)) {
				event.cancelWithResult(ItemInteractionResult.SUCCESS);
			}
		}
	}

	@SubscribeEvent
	public static void useItemInAir(PlayerInteractEvent.RightClickItem event) {
		if (event.getEntity() instanceof Player player) {
			var tool = ToolItem.of(event.getItemStack());

			if (tool != null && tool.use(player, event)) {
				event.setCancellationResult(InteractionResult.SUCCESS);
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void entityInvulnerabilityCheck(EntityInvulnerabilityCheckEvent event) {
		if (!event.getOriginalInvulnerability() && !event.isInvulnerable()) {
			var v = EntityOverride.INVULNERABLE.get(event.getEntity());

			if (v != null && v) {
				event.setInvulnerable(true);
			}
		}
	}
}
