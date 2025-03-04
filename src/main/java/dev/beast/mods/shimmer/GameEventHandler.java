package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.feature.clock.Clock;
import dev.beast.mods.shimmer.feature.clock.ClockEvent;
import dev.beast.mods.shimmer.feature.clock.ClockFont;
import dev.beast.mods.shimmer.feature.clock.SyncClockFontsPayload;
import dev.beast.mods.shimmer.feature.clock.SyncClocksPayload;
import dev.beast.mods.shimmer.feature.cutscene.Cutscene;
import dev.beast.mods.shimmer.feature.structure.StructureStorage;
import dev.beast.mods.shimmer.feature.zone.SyncZonesPayload;
import dev.beast.mods.shimmer.feature.zone.ZoneLoader;
import dev.beast.mods.shimmer.math.KMath;
import dev.beast.mods.shimmer.util.FireworkColors;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.component.FireworkExplosion;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
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
	public static void itemRightClicked(UseItemOnBlockEvent event) {
		if (event.getPlayer() != null && event.getItemStack().has(DataComponents.CUSTOM_DATA)) {
			var toolType = event.getItemStack().get(DataComponents.CUSTOM_DATA).getUnsafe().getString("shimmer:tool");

			if (!toolType.isEmpty()) {
				if (toolType.equals("pos")) {
					if (event.getLevel().isClientSide()) {
						var str = event.getPlayer().isShiftKeyDown() ? KMath.formatVec3(event.getPos().getCenter()) : KMath.formatBlockPos(event.getPos());
						event.getPlayer().tell(Component.literal(str).withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, str)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to Copy")))));
					}
				}

				event.cancelWithResult(ItemInteractionResult.SUCCESS);
			}
		}
	}

	@SubscribeEvent
	public static void clockEvent(ClockEvent event) {
		if (event.getEventName().equals("fireworks") && !event.getLevel().isClientSide()) {
			var list = List.of(
				new FireworkExplosion(FireworkExplosion.Shape.LARGE_BALL, FireworkColors.SUCCESS, FireworkColors.NONE, false, true),
				new FireworkExplosion(FireworkExplosion.Shape.SMALL_BALL, FireworkColors.SUCCESS, FireworkColors.NONE, false, true)
			);

			for (int i = 0; i < 4; i++) {
				event.getLevel().createFireworks(-232.5, 87.5 + i * 4D, -233.5, 0D, 0D, 0D, list);
				event.getLevel().createFireworks(-260.5, 87.5 + i * 4D, -233.5, 0D, 0D, 0D, list);
			}
		}
	}
}
