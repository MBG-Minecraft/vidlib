package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import dev.beast.mods.shimmer.feature.clock.Clock;
import dev.beast.mods.shimmer.feature.clock.ClockFont;
import dev.beast.mods.shimmer.feature.cutscene.Cutscene;
import dev.beast.mods.shimmer.feature.entity.EntityOverride;
import dev.beast.mods.shimmer.feature.item.ShimmerTool;
import dev.beast.mods.shimmer.feature.location.Location;
import dev.beast.mods.shimmer.feature.misc.MarkerData;
import dev.beast.mods.shimmer.feature.session.RemovePlayerDataPayload;
import dev.beast.mods.shimmer.feature.skybox.SkyboxData;
import dev.beast.mods.shimmer.feature.structure.StructureStorage;
import dev.beast.mods.shimmer.feature.zone.ZoneLoader;
import dev.beast.mods.shimmer.util.Empty;
import dev.beast.mods.shimmer.util.registry.BasicShimmerRegistry;
import dev.latvian.mods.kmath.Range;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
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
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.GAME)
public class GameEventHandler {
	public static Range ambientLight = Range.FULL;
	public static boolean gameLoaded = false;

	public static void gameLoaded() {
		if (!gameLoaded) {
			gameLoaded = true;
			AutoInit.Type.GAME_LOADED.invoke();
		}
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
		event.addListener(Shimmer.id("location"), new Location.Loader());
		event.addListener(Shimmer.id("zone"), new ZoneLoader());
		event.addListener(Shimmer.id("structure"), new StructureStorage(StructureStorage.SERVER));
		event.addListener(Shimmer.id("cutscene"), new Cutscene.Loader());
		event.addListener(Shimmer.id("clock_font"), new ClockFont.Loader());
		event.addListener(Shimmer.id("clock"), new Clock.Loader());
		event.addListener(Shimmer.id("skybox"), new SkyboxData.Loader());

		event.addDependency(Shimmer.id("clock_font"), Shimmer.id("clock"));
	}

	@SubscribeEvent
	public static void syncReload(OnDatapackSyncEvent event) {
		if (event.getPlayer() == null) {
			if (ServerLifecycleHooks.getCurrentServer() != null) {
				ServerLifecycleHooks.getCurrentServer().shimmer$clearProfileCache();
			}

			AutoInit.Type.DATA_RELOADED.invoke();

			for (var player : event.getPlayerList().getPlayers()) {
				Shimmer.sync(player, false);
			}
		}
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
			player.server.marker(new MarkerData("player/logged_out", player));
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
	public static void serverStarting(ServerStartingEvent event) {
		gameLoaded();
	}

	@SubscribeEvent
	public static void serverStarted(ServerStartedEvent event) {
		if (ShimmerConfig.betterDefaultGameRules) {
			event.getServer().betterDefaultGameRules();
		}

		AutoInit.Type.SERVER_STARTED.invoke(event.getServer());
	}

	@SubscribeEvent
	public static void serverStopped(ServerStoppedEvent event) {
		BasicShimmerRegistry.releaseServerHolders();
	}

	@SubscribeEvent
	public static void serverPreTick(ServerTickEvent.Pre event) {
		event.getServer().shimmer$preTick(event.getServer().getPauseType());
	}

	@SubscribeEvent
	public static void serverPostTick(ServerTickEvent.Post event) {
		event.getServer().shimmer$postTick(event.getServer().getPauseType());
	}

	@SubscribeEvent
	public static void levelLoaded(LevelEvent.Load event) {
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
			var item = event.getItemStack();
			var tool = ShimmerTool.of(item);

			if (tool != null && tool.useOnBlock(event.getPlayer(), item, event)) {
				event.cancelWithResult(InteractionResult.SUCCESS);
			}
		}
	}

	@SubscribeEvent
	public static void useItemInAir(PlayerInteractEvent.RightClickItem event) {
		var item = event.getItemStack();
		var tool = ShimmerTool.of(item);

		if (tool != null && tool.use(event.getEntity(), item)) {
			event.setCancellationResult(InteractionResult.SUCCESS);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void useItemOnEntity(PlayerInteractEvent.EntityInteract event) {
		var item = event.getItemStack();
		var tool = ShimmerTool.of(item);

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
		var mod = Math.pow(event.getEntity().shimmer$gravityMod(), 2D);

		if (mod <= 0D) {
			event.setCanceled(true);
		} else {
			event.setDamageMultiplier((float) (event.getDamageMultiplier() * mod));
		}
	}

	@SubscribeEvent
	public static void livingDamagePre(LivingDamageEvent.Pre event) {
		var mod = event.getEntity().shimmer$attackDamageMod();

		if (mod != 1F) {
			event.setNewDamage(event.getNewDamage() * mod);
		}
	}

	@SubscribeEvent
	public static void projectileImpact(ProjectileImpactEvent event) {
		if (event.getProjectile() instanceof Projectile && event.getRayTraceResult() instanceof EntityHitResult hit && hit.getEntity() instanceof Player player && player.isCreative()) {
			event.setCanceled(true);
		}
	}
}
