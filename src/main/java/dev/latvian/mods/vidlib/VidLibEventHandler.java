package dev.latvian.mods.vidlib;

import dev.latvian.mods.klib.math.Range;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.core.VLPayloadRegistrar;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.cutscene.Cutscene;
import dev.latvian.mods.vidlib.feature.entity.EntityOverride;
import dev.latvian.mods.vidlib.feature.item.VidLibTool;
import dev.latvian.mods.vidlib.feature.location.Location;
import dev.latvian.mods.vidlib.feature.misc.MarkerData;
import dev.latvian.mods.vidlib.feature.net.S2CPacketBundleBuilder;
import dev.latvian.mods.vidlib.feature.prop.PropRemoveType;
import dev.latvian.mods.vidlib.feature.prop.RemoveAllPropsPayload;
import dev.latvian.mods.vidlib.feature.registry.GenericVLRegistry;
import dev.latvian.mods.vidlib.feature.session.RemovePlayerDataPayload;
import dev.latvian.mods.vidlib.feature.structure.StructureStorage;
import dev.latvian.mods.vidlib.feature.zone.Anchor;
import dev.latvian.mods.vidlib.feature.zone.ZoneLoader;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@EventBusSubscriber(modid = VidLib.ID)
public class VidLibEventHandler {
	public static Range ambientLight = Range.FULL;
	public static boolean gameLoaded = false;

	public static synchronized void gameLoaded() {
		if (!gameLoaded) {
			gameLoaded = true;
			AutoInit.Type.GAME_LOADED.invoke();
		}
	}

	@SubscribeEvent
	public static void afterLoad(FMLLoadCompleteEvent event) {
		gameLoaded();
	}

	@SubscribeEvent
	public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
		var reg = VLPayloadRegistrar.of(event);

		for (var s : AutoPacket.SCANNED.get()) {
			if (s.to().contains(AutoPacket.To.CLIENT) && s.to().contains(AutoPacket.To.SERVER)) {
				reg.bidi(s.type());
			} else if (s.to().contains(AutoPacket.To.CLIENT)) {
				reg.s2c(s.type());
			} else if (s.to().contains(AutoPacket.To.SERVER)) {
				reg.c2s(s.type());
			}
		}
	}

	@SubscribeEvent
	public static void buildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.OP_BLOCKS) {
			for (var entry : VidLibTool.REGISTRY.get().entrySet()) {
				event.accept(entry.getValue().createFullItem());
			}

			for (var item : BuiltInRegistries.ITEM) {
				var mod = item.builtInRegistryHolder().getKey().location().getNamespace();

				if (mod.equals("video") || mod.equals(VidLib.ID)) {
					event.accept(item.getDefaultInstance());
				}
			}
		}
	}

	@SubscribeEvent
	public static void registerTicketControllers(RegisterTicketControllersEvent event) {
		event.register(Anchor.TICKET_CONTROLLER);
	}

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
	public static void addReloadListeners(AddServerReloadListenersEvent event) {
		event.addListener(VidLib.id("location"), new Location.Loader(Location.REGISTRY));
		event.addListener(VidLib.id("zone"), new ZoneLoader(ZoneLoader.SERVER_BY_DIMENSION, true));
		event.addListener(VidLib.id("structure"), new StructureStorage(StructureStorage.SERVER));
		event.addListener(VidLib.id("cutscene"), new Cutscene.Loader());
	}

	@SubscribeEvent
	public static void syncReload(OnDatapackSyncEvent event) {
		if (event.getPlayer() == null) {
			if (ServerLifecycleHooks.getCurrentServer() != null) {
				ServerLifecycleHooks.getCurrentServer().vl$clearProfileCache();
			}

			AutoInit.Type.DATA_LOADED.invoke();

			for (var player : event.getPlayerList().getPlayers()) {
				VidLib.sync(player, 0);
			}
		}
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			player.server.vl$playerJoined(player);
		}
	}

	@SubscribeEvent
	public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			player.server.marker(new MarkerData("player/logged_out", player));
			player.server.s2c(new RemovePlayerDataPayload(player.getUUID()));
		}
	}

	@SubscribeEvent
	public static void playerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			var level = event.getEntity().level();
			var packets = new S2CPacketBundleBuilder(level);

			for (var list : level.getProps().propLists.values()) {
				packets.s2c(new RemoveAllPropsPayload(list.type, PropRemoveType.DIMENSION_CHANGE));

				for (var prop : list) {
					packets.s2c(prop.createAddPacket());
				}
			}

			packets.send(player);
		}
	}

	@SubscribeEvent
	public static void playerSaved(PlayerEvent.SaveToFile event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			player.vl$sessionData().dataMap.save(player.server, player.server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve("vidlib").resolve(player.getUUID() + ".nbt"));
		}
	}

	@SubscribeEvent
	public static void serverStarting(ServerStartingEvent event) {
		gameLoaded();
	}

	@SubscribeEvent
	public static void serverStarted(ServerStartedEvent event) {
		if (VidLibConfig.betterDefaultGameRules) {
			event.getServer().betterDefaultGameRules();
		}

		AutoInit.Type.SERVER_STARTED.invoke(event.getServer());
	}

	@SubscribeEvent
	public static void serverStopped(ServerStoppedEvent event) {
		GenericVLRegistry.releaseServerHolders();
	}

	@SubscribeEvent
	public static void serverPreTick(ServerTickEvent.Pre event) {
		event.getServer().vl$preTick(event.getServer().getPauseType());
	}

	@SubscribeEvent
	public static void serverPostTick(ServerTickEvent.Post event) {
		event.getServer().vl$postTick(event.getServer().getPauseType());
	}

	@SubscribeEvent
	public static void levelLoaded(LevelEvent.Load event) {
	}

	@SubscribeEvent
	public static void levelSaved(LevelEvent.Save event) {
		if (event.getLevel() instanceof ServerLevel level && level.dimension() == Level.OVERWORLD) {
			level.getServer().getServerData().save(level.getServer(), level.getServer().getWorldPath(LevelResource.ROOT).resolve("vidlib.nbt"));
		}
	}

	@SubscribeEvent
	public static void useItemOnBlock(UseItemOnBlockEvent event) {
		if (event.getPlayer() != null) {
			var item = event.getItemStack();
			var tool = VidLibTool.of(item);

			if (tool != null && tool.useOnBlock(event.getPlayer(), item, event)) {
				event.cancelWithResult(InteractionResult.SUCCESS);
			}
		}
	}

	@SubscribeEvent
	public static void useItemInAir(PlayerInteractEvent.RightClickItem event) {
		var item = event.getItemStack();
		var tool = VidLibTool.of(item);

		if (tool != null && tool.use(event.getEntity(), item)) {
			event.setCancellationResult(InteractionResult.SUCCESS);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void useItemOnEntity(PlayerInteractEvent.EntityInteract event) {
		var item = event.getItemStack();
		var tool = VidLibTool.of(item);

		if (tool != null && tool.useOnEntity(event.getEntity(), item, event.getTarget())) {
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
	public static void tabName(PlayerEvent.TabListNameFormat event) {
		var nickname = event.getEntity().getNickname();

		if (!Empty.isEmpty(nickname)) {
			event.setDisplayName(nickname);
		}
	}

	@SubscribeEvent
	public static void livingFall(LivingFallEvent event) {
		var mod = Math.pow(event.getEntity().vl$gravityMod(), 2D);

		if (mod <= 0D) {
			event.setCanceled(true);
		} else {
			event.setDamageMultiplier((float) (event.getDamageMultiplier() * mod));
		}
	}

	@SubscribeEvent
	public static void livingDamagePre(LivingDamageEvent.Pre event) {
		var mod = event.getEntity().vl$attackDamageMod();

		if (mod != 1F) {
			event.setNewDamage(event.getNewDamage() * mod);
		}
	}

	@SubscribeEvent
	public static void projectileImpact(ProjectileImpactEvent event) {
		if (event.getRayTraceResult() instanceof EntityHitResult hit && hit.getEntity() instanceof Player player && player.isCreative()) {
			event.setCanceled(true);
		}

		if (event.getRayTraceResult() instanceof BlockHitResult hit && event.getProjectile().level().getBlockState(hit.getBlockPos()).is(Blocks.BARRIER)) {
			event.setCanceled(true);
		}
	}
}
