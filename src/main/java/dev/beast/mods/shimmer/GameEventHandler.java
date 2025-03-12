package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import dev.beast.mods.shimmer.feature.clock.Clock;
import dev.beast.mods.shimmer.feature.clock.ClockFont;
import dev.beast.mods.shimmer.feature.clock.ClockInstance;
import dev.beast.mods.shimmer.feature.clock.SyncClockFontsPayload;
import dev.beast.mods.shimmer.feature.clock.SyncClocksPayload;
import dev.beast.mods.shimmer.feature.cutscene.Cutscene;
import dev.beast.mods.shimmer.feature.entity.EntityOverride;
import dev.beast.mods.shimmer.feature.misc.InternalPlayerData;
import dev.beast.mods.shimmer.feature.session.RemovePlayerDataPayload;
import dev.beast.mods.shimmer.feature.structure.StructureStorage;
import dev.beast.mods.shimmer.feature.toolitem.ShimmerTool;
import dev.beast.mods.shimmer.feature.zone.SyncZonesPayload;
import dev.beast.mods.shimmer.feature.zone.ZoneLoader;
import dev.beast.mods.shimmer.util.S2CPacketBundleBuilder;
import dev.beast.mods.shimmer.util.registry.RegistryReference;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.GAME)
public class GameEventHandler {
	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event) {
		for (var s : AutoRegister.SCANNED.get()) {
			if (s.value() instanceof ServerCommandHolder(String name, ServerCommandHolder.Callback callback)) {
				var command = Commands.literal(name);
				callback.register(command, event.getBuildContext());
				event.getDispatcher().register(command);
			}
		}
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
		var list = new S2CPacketBundleBuilder();

		list.s2c(new SyncZonesPayload(List.copyOf(ZoneLoader.ALL.containers.values())).toS2C());
		list.s2c(new SyncClockFontsPayload(List.copyOf(ClockFont.SERVER.getMap().values())).toS2C());
		list.s2c(new SyncClocksPayload(List.copyOf(ClockInstance.SERVER.getMap().values())).toS2C());

		event.getRelevantPlayers().forEach(list::send);
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			player.server.shimmer$playerJoined(player);
		}
	}

	@SubscribeEvent
	public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			player.server.s2c(new RemovePlayerDataPayload(player.getUUID()));
		}
	}

	@SubscribeEvent
	public static void playerSaved(PlayerEvent.SaveToFile event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			player.shimmer$sessionData().dataMap.save(player.server, player.server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve("shimmer").resolve(player.getUUID() + ".nbt"));
		}
	}

	@SubscribeEvent
	public static void serverStarted(ServerStartedEvent event) {
		if (ShimmerConfig.betterDefaultGameRules) {
			event.getServer().betterDefaultGameRules();
		}
	}

	@SubscribeEvent
	public static void serverStopped(ServerStoppedEvent event) {
		RegistryReference.releaseServerHolders();
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
	public static void levelSaved(LevelEvent.Save event) {
		if (event.getLevel() instanceof ServerLevel level && level.dimension() == Level.OVERWORLD) {
			level.getServer().getServerData().save(level.getServer(), level.getServer().getWorldPath(LevelResource.ROOT).resolve("shimmer.nbt"));
		}
	}

	@SubscribeEvent
	public static void useItemOnBlock(UseItemOnBlockEvent event) {
		if (event.getPlayer() != null) {
			var tool = ShimmerTool.of(event.getItemStack());

			if (tool != null && tool.useOnBlock(event.getPlayer(), event)) {
				event.cancelWithResult(ItemInteractionResult.SUCCESS);
			}
		}
	}

	@SubscribeEvent
	public static void useItemInAir(PlayerInteractEvent.RightClickItem event) {
		var tool = ShimmerTool.of(event.getItemStack());

		if (tool != null && tool.use(event.getEntity(), event)) {
			event.setCancellationResult(InteractionResult.SUCCESS);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void useItemOnEntity(PlayerInteractEvent.EntityInteract event) {
		var tool = ShimmerTool.of(event.getItemStack());

		if (tool != null && tool.useOnEntity(event.getEntity(), event)) {
			event.setCancellationResult(InteractionResult.SUCCESS);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void entityInvulnerabilityCheck(EntityInvulnerabilityCheckEvent event) {
		if (!event.getOriginalInvulnerability() && !event.isInvulnerable() && EntityOverride.INVULNERABLE.get(event.getEntity(), false)) {
			event.setInvulnerable(true);
		}
	}

	@SubscribeEvent
	public static void name(PlayerEvent.NameFormat event) {
		var data = event.getEntity().get(InternalPlayerData.NICKNAME);

		if (!data.getString().isEmpty()) {
			event.setDisplayname(data);
		}
	}

	@SubscribeEvent
	public static void tabName(PlayerEvent.TabListNameFormat event) {
		var data = event.getEntity().get(InternalPlayerData.NICKNAME);

		if (!data.getString().isEmpty()) {
			event.setDisplayName(data);
		}
	}

	@SubscribeEvent
	public static void livingFall(LivingFallEvent event) {
		var mod = Math.pow(event.getEntity().shimmer$gravityMod(), 2D);

		if (mod <= 0D) {
			event.setCanceled(true);
		} else {
			event.setDamageMultiplier((float) (event.getDamageMultiplier() * mod));
		}
	}
}
