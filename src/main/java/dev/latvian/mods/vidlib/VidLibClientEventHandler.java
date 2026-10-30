package dev.latvian.mods.vidlib;

import com.mojang.blaze3d.platform.InputConstants;
import dev.latvian.mods.betteradvancedtooltips.BATIcons;
import dev.latvian.mods.klib.entity.EntityUtils;
import dev.latvian.mods.klib.platform.PlatformHelper;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.klib.util.StringUtils;
import dev.latvian.mods.replay.api.ReplayAPI;
import dev.latvian.mods.vidlib.client.VidLibWorldRenderState;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.auto.BlockEntityRendererHolder;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ClientCommandHolder;
import dev.latvian.mods.vidlib.feature.auto.EntityRendererHolder;
import dev.latvian.mods.vidlib.feature.client.VidLibClientOptions;
import dev.latvian.mods.vidlib.feature.client.VidLibEntityRenderStates;
import dev.latvian.mods.vidlib.feature.client.VidLibHUD;
import dev.latvian.mods.vidlib.feature.client.VidLibKeys;
import dev.latvian.mods.vidlib.feature.clothing.ClothingPresetLoader;
import dev.latvian.mods.vidlib.feature.data.InternalServerData;
import dev.latvian.mods.vidlib.feature.dynamicresources.DynamicResourceEvent;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import dev.latvian.mods.vidlib.feature.item.RainbowItemTint;
import dev.latvian.mods.vidlib.feature.item.VidLibTool;
import dev.latvian.mods.vidlib.feature.misc.ClientModListPayload;
import dev.latvian.mods.vidlib.feature.misc.DebugTextEvent;
import dev.latvian.mods.vidlib.feature.misc.MainMenuOpenedEvent;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import dev.latvian.mods.vidlib.feature.misc.ScreenText;
import dev.latvian.mods.vidlib.feature.misc.ScreenTextRenderer;
import dev.latvian.mods.vidlib.feature.particle.VidLibClientParticles;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleData;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleManager;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import dev.latvian.mods.vidlib.feature.prop.ClientProps;
import dev.latvian.mods.vidlib.feature.prop.PropHitResult;
import dev.latvian.mods.vidlib.feature.skybox.SkyboxData;
import dev.latvian.mods.vidlib.feature.structure.GhostStructure;
import dev.latvian.mods.vidlib.feature.structure.StructureCapture;
import dev.latvian.mods.vidlib.feature.structure.StructureRenderer;
import dev.latvian.mods.vidlib.feature.structure.StructureStorage;
import dev.latvian.mods.vidlib.feature.visual.Visuals;
import dev.latvian.mods.vidlib.feature.zone.Anchor;
import dev.latvian.mods.vidlib.feature.zone.ZoneLoader;
import dev.latvian.mods.vidlib.util.NameDrawType;
import dev.mrbeastgaming.mods.hub.api.HubClientSession;
import dev.mrbeastgaming.mods.hub.api.data.HubFileType;
import dev.mrbeastgaming.mods.hub.api.data.HubUserCapabilities;
import dev.mrbeastgaming.mods.hub.api.gateway.HubClientGateway;
import dev.mrbeastgaming.mods.hub.client.LinkHubUserScreen;
import dev.mrbeastgaming.mods.hub.client.LinkMinecraftScreen;
import dev.mrbeastgaming.mods.hub.event.SyncClientFilesHubEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.CalculateDetachedCameraDistanceEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientResourceLoadFinishedEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.FrameGraphSetupEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.client.event.ToastAddEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.GameShuttingDownEvent;

import java.nio.file.Files;
import java.util.List;

@EventBusSubscriber(modid = VidLib.ID, value = Dist.CLIENT)
public class VidLibClientEventHandler {
	public static boolean clientLoaded = false;

	private static final List<String> REMOVE_RIGHT = List.of(
		"CPU: ",
		"Display: ",
		"Sodium Renderer ",
		"Geometry Pool: ",
		"Transfer Queue: ",
		"Chunk Builder: ",
		"Chunk Queues: "
	);

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(VidLibClientEventHandler::syncSetup);
	}

	public static void syncSetup() {
		InternalServerData.ANCHOR.addUpdateListener(VidLibClientEventHandler::updateAnchor);
	}

	private static void updateAnchor(Player player, Anchor anchor) {
		Anchor.client = anchor;
	}

	@SubscribeEvent
	public static void dynamicResources(DynamicResourceEvent.Assets event) {
		event.register(ID.vidlib("dynamic_resources/clothing"));
	}

	@SubscribeEvent
	public static void addReloadListeners(AddClientReloadListenersEvent event) {
		event.addListener(ID.vidlib("structure"), new StructureStorage(StructureStorage.CLIENT));
		event.addListener(ID.vidlib("ghost_structure"), new GhostStructure.Loader());
		event.addListener(ID.vidlib("clothing_preset"), new ClothingPresetLoader());
		event.addListener(ID.vidlib("physics_particle_data"), new PhysicsParticleData.Loader());
		event.addListener(ID.vidlib("skybox"), new SkyboxData.Loader());
		event.addListener(ID.vidlib("zone"), new ZoneLoader(ZoneLoader.CLIENT_BY_DIMENSION, false));

		event.addDependency(ID.vidlib("gradient"), ID.vidlib("clothing_preset"));
		event.addDependency(ID.vidlib("structure"), ID.vidlib("ghost_structure"));
		event.addDependency(ID.vidlib("clock_font"), ID.vidlib("clock"));
	}

	@SubscribeEvent
	public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
		VidLibClientParticles.register(event);
	}

	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		for (var s : ClientAutoRegister.SCANNED.get()) {
			if (s.value() instanceof EntityRendererHolder<?> holder) {
				holder.register(event);
			} else if (s.value() instanceof BlockEntityRendererHolder<?, ?> holder) {
				holder.register(event);
			}
		}
	}

	@SubscribeEvent
	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		VidLibKeys.register(event);
	}

	@SubscribeEvent
	public static void registerGuiLayers(RegisterGuiLayersEvent event) {
		event.registerBelowAll(ID.vidlib("below_all"), VidLibHUD::drawBelowAll);
		event.registerAbove(VanillaGuiLayers.BOSS_OVERLAY, ID.vidlib("above_boss"), VidLibHUD::drawAboveBossOverlay);
		event.registerAbove(VanillaGuiLayers.OVERLAY_MESSAGE, ID.vidlib("information_hud"), VidLibHUD::drawInformationHUD);
		event.registerAboveAll(ID.vidlib("above_all"), VidLibHUD::drawAboveAll);
	}

	@SubscribeEvent
	public static void registerItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
		event.register(ID.vidlib("rgb"), RainbowItemTint.MAP_CODEC);
	}

	@SubscribeEvent
	public static void loggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
		HubClientGateway.ifPresent(HubClientGateway::updateInfo);
	}

	@SubscribeEvent
	public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
		HubClientGateway.ifPresent(gateway -> MiscClientUtils.delayedExecute(gateway.mc, gateway::updateInfo));
	}

	@SubscribeEvent
	public static void clientPreTick(ClientTickEvent.Pre event) {
		VidLibEventHandler.gameLoaded();

		if (!clientLoaded) {
			clientLoaded = true;
			AutoInit.Type.CLIENT_LOADED.invoke();
		}

		Visuals.TICK_DEBUG.clear();
		ScreenText.CLIENT_TICK.clear();
		var mc = Minecraft.getInstance();

		if (mc.level != null && mc.player != null) {
			if (VidLibClient.levelTick == Integer.MAX_VALUE) {
				VidLibClient.levelTick = 0;
			} else {
				VidLibClient.levelTick++;
			}

			ScreenText.CLIENT_TICK.ops = mc.jsonOps();

			var tool = VidLibTool.of(mc.player);

			if (tool != null) {
				tool.getSecond().debugText(mc.player, tool.getFirst(), mc.hitResult, ScreenText.CLIENT_TICK);
			}

			mc.vl$preTick(mc.getPauseType());
		}

		VidLibKeys.handle(mc);
	}

	@SubscribeEvent
	public static void clientPostTick(ClientTickEvent.Post event) {
		var mc = Minecraft.getInstance();
		mc.vl$postTick(mc.getPauseType());
		NeoForge.EVENT_BUS.post(new DebugTextEvent.ClientTick(ScreenText.CLIENT_TICK));

		int b = StructureCapture.CURRENT.getValue().blocks.size();

		if (b > 0) {
			ScreenText.CLIENT_TICK.topLeft.add("Ghost Structure Capture: %,d blocks".formatted(b));
		}

		if (!MiscClientUtils.CLIENT_CLOSEABLE.isEmpty()) {
			for (var c : MiscClientUtils.CLIENT_CLOSEABLE) {
				try {
					c.close();
				} catch (Exception ignored) {
				}
			}

			MiscClientUtils.CLIENT_CLOSEABLE.clear();
		}

		HubClientGateway.tickGateway();
	}

	@SubscribeEvent
	public static void keyInput(InputEvent.Key event) {
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void preFrameGraphSetup(FrameGraphSetupEvent event) {
		Minecraft.getInstance().vl$renderSetup(event);
	}

	@SubscribeEvent
	public static void extract(ExtractLevelRenderStateEvent event) {
		var state = new VidLibWorldRenderState();

		if (state.extract(event)) {
			event.getRenderState().setRenderData(VidLibEntityRenderStates.WORLD_KEY, state);
		}
	}

	@SubscribeEvent
	public static void submit(SubmitCustomGeometryEvent event) {
		var state = event.getLevelRenderState().getRenderData(VidLibEntityRenderStates.WORLD_KEY);

		if (state != null) {
			state.submit(event);
		}
	}

	@SubscribeEvent
	public static void renderHUD(RenderGuiEvent.Post event) {
		var mc = Minecraft.getInstance();

		if (mc.level != null && mc.player != null) {
			renderHUD0(mc, event);
		}

		ScreenText.RENDER.clear();
	}

	public static void renderHUD0(Minecraft mc, RenderGuiEvent.Post event) {
		var session = mc.player.vl$sessionData();
		var graphics = event.getGuiGraphics();
		var delta = event.getPartialTick().getGameTimeDeltaPartialTick(true);
		int width = event.getGuiGraphics().guiWidth();
		int height = event.getGuiGraphics().guiHeight();

		int renderingStructures = StructureRenderer.getRenderingAll();

		if (renderingStructures != 0) {
			if (!ReplayAPI.getActive().isExporting()) {
				var component = Component.empty().append(BATIcons.ERROR).append(BATIcons.SMALL_SPACE).append("Rendering " + renderingStructures + " structures...");

				if (mc.player.isReplayCamera()) {
					int x = 1;
					int y = 2;
					graphics.fill(x, y, x + mc.font.width(component) + 3, y + 11, 0xA0000000);
					graphics.text(mc.font, component, x + 2, y + 2, 0xFFFFFFFF, true);
				} else {
					ScreenText.RENDER.topLeft.add(component);
				}
			}
		}

		boolean primitiveF3Open = mc.gui.hud.getDebugOverlay().showDebugScreen() && ClientGameEngine.INSTANCE.primitiveF3(mc);

		if ((primitiveF3Open || VidLibClientOptions.getShowCoordinates()) && ClientGameEngine.INSTANCE.allowCoordinateDisplay(mc)) {
			var pos = mc.gameRenderer.mainCamera().position();
			var x = Component.literal("%.01f".formatted(pos.x)).withColor(0xFF7070);
			var y = Component.literal("%.01f".formatted(pos.y - mc.player.getEyeHeight())).withColor(0x7CFF70);
			var z = Component.literal("%.01f".formatted(pos.z)).withColor(0x70BCFF);
			var yaw = Component.literal("%.01f".formatted(Mth.wrapDegrees(mc.player.getViewYRot(delta)))).withColor(0xFFD870);
			var pitch = Component.literal("%.01f".formatted(Mth.wrapDegrees(mc.player.getViewXRot(delta)))).withColor(0xFF9870);

			var comp = Component.empty();
			comp.append(x);
			comp.append(" ");
			comp.append(y);
			comp.append(" ");
			comp.append(z);
			comp.append(" " + StringUtils.snakeCaseToTitleCase(mc.player.getDirection().getSerializedName()) + " (");
			comp.append(yaw);
			comp.append(" ");
			comp.append(pitch);
			comp.append(")");

			ScreenText.RENDER.topLeft.add(comp);
		}

		if (!ClientGameEngine.INSTANCE.hideGui(mc) && !mc.player.isReplayCamera()) {
			ScreenText.RENDER.addAll(ScreenText.CLIENT_TICK);
			ScreenText.RENDER.ops = mc.jsonOps();

			if (mc.gui.screen() == null || mc.gui.screen() instanceof ChatScreen) {
				NeoForge.EVENT_BUS.post(new DebugTextEvent.Render(ScreenText.RENDER));

				var zoneClip = session.zoneClip;

				if (zoneClip != null) {
					var component = Component.literal("Zone: ").append(Component.literal(zoneClip.zone().container.id.toString()).withStyle(ChatFormatting.AQUA));

					if (zoneClip.zone().container.zones.size() > 1) {
						component.append(Component.literal("[" + zoneClip.zone().index + "]").withStyle(ChatFormatting.GREEN));
					}

					if (zoneClip.zone().container.isGenerated()) {
						component.append(Component.literal("*").withStyle(ChatFormatting.LIGHT_PURPLE));
					}

					ScreenText.RENDER.topLeft.add(component);

					var zoneTag = zoneClip.zone().volume.data();

					if (!zoneTag.isEmpty()) {
						for (var key : zoneTag.keySet()) {
							ScreenText.RENDER.topLeft.add(Component.literal(key + ": ").append(NbtUtils.toPrettyComponent(zoneTag.get(key))));
						}
					}
				}

				if (!session.zonesTagsIn.isEmpty() && VidLibClientOptions.getShowZones()) {
					ScreenText.RENDER.topRight.add("Zones in:");

					for (var tag : session.zonesTagsIn) {
						ScreenText.RENDER.topRight.add(tag);
					}
				}

				;
				graphics.pose().translate(0F, 0F);

				if (mc.gui.screen() instanceof ChatScreen) {
					ScreenTextRenderer.render(graphics, ScreenText.RENDER, mc.font, 0, 0, width, height - 14, 0x40000000, 0x70FFFFFF);
				} else {
					ScreenTextRenderer.render(graphics, ScreenText.RENDER, mc.font, 0, 0, width, height, 0xA0000000, 0xFFFFFFFF);
				}

				;
			}
		}
	}

	@SubscribeEvent
	public static void adjustFOV(ViewportEvent.ComputeFov event) {
		var mc = Minecraft.getInstance();
		var override = ClientGameEngine.INSTANCE.overrideCamera(mc);

		if (override != null) {
			event.setFOV((float) (event.getFOV() * override.getFOVModifier(event.getPartialTick())));
		}

		/*
		if (mc.player != null && mc.player.getVehicle() != null) {
			event.setFOV(event.getFOV() * mc.player.getVehicle().adjustFOV(camera, event.getPartialTick(), changingFov));
		}
		 */
	}

	/**
	 * Replacement for NeoForge 1.21.5's {@code RenderHighlightEvent.Block}, which no longer exists in
	 * 26.3: the block outline is now submitted through {@code LevelRenderer#submitBlockOutline}, so
	 * this hook is driven by {@code LevelRendererMixin} with the outlined block position.
	 *
	 * @return {@code true} when the vanilla block outline must be suppressed
	 */
	public static boolean renderBlockHighlight(BlockPos blockPos) {
		var mc = Minecraft.getInstance();

		if (mc.player != null && mc.level != null) {
			if (!EntityUtils.isSpectatorOrCreative(mc.player) && mc.level.getBlockState(blockPos).is(Blocks.BARRIER)) {
				return true;
			}

			var tool = VidLibTool.of(mc.player);

			if (tool != null) {
				tool.getSecond().visuals(mc.player, tool.getFirst(), Visuals.TEMP, mc.getDeltaTracker().getGameTimeDeltaPartialTick(true));

				if (Visuals.TEMP.contains(blockPos)) {
					return true;
				}

				Visuals.TEMP.clear();
			}
		}

		return false;
	}

	/**
	 * Replacement for NeoForge 1.21.5's {@code CustomizeGuiOverlayEvent.DebugText}, which no longer
	 * exists in 26.3: the debug overlay is entry-driven and no longer exposes modifiable line lists,
	 * so this hook is driven by {@code DebugScreenOverlayMixin} with the vanilla lists instead.
	 */
	public static void debugText(List<String> left, List<String> right) {
		var mc = Minecraft.getInstance();

		if (ClientGameEngine.INSTANCE.primitiveF3(mc)) {
			left.clear();
			right.clear();
			return;
		}

		right.removeIf(s -> {
			for (var r : REMOVE_RIGHT) {
				if (ChatFormatting.stripFormatting(s).startsWith(r)) {
					return true;
				}
			}

			return false;
		});

		PhysicsParticleManager.debugInfo(left::add, right::add);
	}

	@SubscribeEvent
	public static void registerClientCommands(RegisterClientCommandsEvent event) {
		for (var s : ClientAutoRegister.SCANNED.get()) {
			if (s.value() instanceof ClientCommandHolder(String name, ClientCommandHolder.Callback callback)) {
				var command = Commands.literal(name);
				callback.register(command, event.getBuildContext());
				event.getDispatcher().register(command);
			}
		}
	}

	@SubscribeEvent
	public static void addToast(ToastAddEvent event) {
		var toast = event.getToast();

		if (ClientGameEngine.INSTANCE.disableToast(event.getToast())) {
			event.setCanceled(true);
		} else if (toast instanceof SystemToast systemToast) {
			var t = systemToast.getToken();

			if (t == SystemToast.SystemToastId.UNSECURE_SERVER_WARNING) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void renderPlayerPre(RenderPlayerEvent.Pre<?> event) {
		if (event.getRenderState().isSpectator) {
			event.setCanceled(true);
			return;
		}

		if (event.getRenderState().isInvisible) {
			if (VidLibEntityRenderStates.isCreative(event.getRenderState())) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void interactionKeyMappingTriggered(InputEvent.InteractionKeyMappingTriggered event) {
		var mc = Minecraft.getInstance();

		if (mc.player != null && event.isAttack()) {
			if (mc.player.vl$isSuspended()) {
				event.setCanceled(true);
				return;
			}

			var item = mc.player.getItemInHand(event.getHand());
			var tool = VidLibTool.of(item);

			if (tool != null && tool.leftClick(mc.player, item)) {
				event.setSwingHand(true);
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void renderInventoryMobEffects(ScreenEvent.RenderInventoryMobEffects event) {
		event.setCompact(true);
	}

	@SubscribeEvent
	public static void calculateDetachedCameraDistance(CalculateDetachedCameraDistanceEvent event) {
		var mc = Minecraft.getInstance();
		var vehicle = mc.player != null ? mc.player.getVehicle() : null;

		if (vehicle != null) {
			event.setDistance(vehicle.getVehicleCameraDistance(mc.player, event.getDistance()));
		}
	}

	@SubscribeEvent
	public static void loggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
		PlayerProfiles.cache(event.getPlayer().getGameProfile());

		if (!event.getPlayer().vl$sessionData().clientModListSentDuringConfig) {
			event.getPlayer().c2s(new ClientModListPayload(PlatformHelper.CURRENT.getModList()));
		}
	}

	@SubscribeEvent
	public static void loggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
		AutoInit.Type.ASSETS_CLOSED.invoke();
	}

	@SubscribeEvent
	public static void clientResourceLoadFinished(ClientResourceLoadFinishedEvent event) {
		var mc = Minecraft.getInstance();
		mc.vl$clearProfileCache();
		AutoInit.Type.ASSETS_LOADED.invoke(mc.getResourceManager());
	}

	@SubscribeEvent
	public static void canRenderNameTag(RenderNameTagEvent.CanRender event) {
		var mc = Minecraft.getInstance();

		if (event.getEntity() instanceof Player && mc.getNameDrawType() != NameDrawType.VANILLA) {
			event.setCanRender(TriState.FALSE);
		}
	}

	@SubscribeEvent
	public static void mouseClicked(InputEvent.MouseButton.Pre event) {
		if (event.getAction() == InputConstants.PRESS) {
			var mc = Minecraft.getInstance();

			if (mc.player != null && mc.gui.screen() == null && mc.isWindowActive() && mc.hitResult instanceof PropHitResult hit && !ClientProps.isPropHidden(hit.prop)) {
				boolean cancel = hit.prop.onClientInteraction(mc.player, event.getButton(), hit.getLocation(), hit.getDirection());

				if (cancel) {
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	public static void computeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
		var mc = Minecraft.getInstance();

		if (mc.player != null && mc.player.getVehicle() != null) {
			event.setRoll(mc.player.getVehicle().getPassengerCameraRoll(mc.player, event.getPartialTick(), event.getRoll()));
		}
	}

	@SubscribeEvent
	public static void mainMenuOpened(MainMenuOpenedEvent event) {
		if (HubUserCapabilities.get().autoUploadFiles()) {
			VidLibClient.checkFileSync(event.isFirstTime());
		}

		var session = HubClientSession.CURRENT;

		if (session.user == null) {
			if (HubUserCapabilities.get().resolveRequireLink()) {
				LinkHubUserScreen.open(event.getMinecraft());
			}
		} else if (session.minecraftLink == null) {
			if (!PlatformHelper.CURRENT.isDevEnv()) {
				LinkMinecraftScreen.handle(event.getMinecraft(), true);
			}
		}
	}

	@SubscribeEvent
	public static void gameShuttingDown(GameShuttingDownEvent event) {
		VidLib.LOGGER.info("Shutting down");
		HubClientGateway.stopGateway();
	}

	@SubscribeEvent
	public static void syncClientFilesHub(SyncClientFilesHubEvent event) {
		event.add(uploads -> {
			var gameDir = uploads.getGameDirectory();
			uploads.addDirectory(gameDir.resolve("voicechat_recordings"), ".mp3", HubFileType.VOICE_CHAT_RECORDING);
			uploads.addDirectory(gameDir.resolve("crash-reports"), "-client.txt", HubFileType.CLIENT_CRASH_REPORT);
			uploads.addDirectory(gameDir.resolve("crash-reports"), "-server.txt", HubFileType.SERVER_CRASH_REPORT);
			uploads.addDirectory(gameDir.resolve("crash-reports"), "-fml.txt", HubFileType.MOD_LOADER_CRASH_REPORT);

			try (var stream = Files.list(gameDir)) {
				for (var file : stream.toList()) {
					var name = file.getFileName().toString();

					if (name.startsWith("hr_err_pid_") && name.endsWith(".log")) {
						uploads.addFile(file, HubFileType.CLIENT_JVM_CRASH_REPORT);
					}
				}
			}
		});
	}
}
