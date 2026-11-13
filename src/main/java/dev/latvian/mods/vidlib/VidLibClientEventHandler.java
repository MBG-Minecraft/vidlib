package dev.latvian.mods.vidlib;

import com.mojang.blaze3d.platform.InputConstants;
import dev.latvian.mods.klib.math.HitBuilder;
import dev.latvian.mods.klib.math.ProjectedCoordinates;
import dev.latvian.mods.klib.platform.PlatformHelper;
import dev.latvian.mods.klib.registry.CustomRegistryUpdateCallback;
import dev.latvian.mods.klib.registry.RefType;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.klib.util.StringUtils;
import dev.latvian.mods.vidlib.feature.atmosphere.Atmosphere;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.auto.BlockEntityRendererHolder;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ClientCommandHolder;
import dev.latvian.mods.vidlib.feature.auto.EntityRendererHolder;
import dev.latvian.mods.vidlib.feature.client.VLWorldRenderState;
import dev.latvian.mods.vidlib.feature.client.VidLibClientOptions;
import dev.latvian.mods.vidlib.feature.client.VidLibEntityRenderStates;
import dev.latvian.mods.vidlib.feature.client.VidLibHUD;
import dev.latvian.mods.vidlib.feature.client.VidLibKeys;
import dev.latvian.mods.vidlib.feature.clothing.ClothingPresetLoader;
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
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import dev.latvian.mods.vidlib.feature.prop.ClientProps;
import dev.latvian.mods.vidlib.feature.prop.PropHitResult;
import dev.latvian.mods.vidlib.feature.prop.PropRenderStateRegistryEvent;
import dev.latvian.mods.vidlib.feature.prop.builtin.danger.DangerProp;
import dev.latvian.mods.vidlib.feature.prop.builtin.danger.DangerPropRenderState;
import dev.latvian.mods.vidlib.feature.prop.builtin.decal.DecalProp;
import dev.latvian.mods.vidlib.feature.prop.builtin.decal.DecalPropRenderState;
import dev.latvian.mods.vidlib.feature.prop.builtin.image.ImageProp;
import dev.latvian.mods.vidlib.feature.prop.builtin.image.ImagePropRenderState;
import dev.latvian.mods.vidlib.feature.prop.builtin.mannequin.MannequinProp;
import dev.latvian.mods.vidlib.feature.prop.builtin.mannequin.MannequinPropRenderState;
import dev.latvian.mods.vidlib.feature.prop.builtin.shape.ShapeProp;
import dev.latvian.mods.vidlib.feature.prop.builtin.shape.ShapePropRenderState;
import dev.latvian.mods.vidlib.feature.prop.builtin.text.TextProp;
import dev.latvian.mods.vidlib.feature.prop.builtin.text.TextPropRenderState;
import dev.latvian.mods.vidlib.feature.prop.builtin.tv.TVProp;
import dev.latvian.mods.vidlib.feature.prop.builtin.tv.TVPropRenderState;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffectRenderingRegistryEvent;
import dev.latvian.mods.vidlib.feature.screeneffect.effect.AngledChromaticAberrationEffect;
import dev.latvian.mods.vidlib.feature.screeneffect.effect.FocusedChromaticAberrationEffect;
import dev.latvian.mods.vidlib.feature.screeneffect.effect.renderer.AngledChromaticAberrationEffectRenderer;
import dev.latvian.mods.vidlib.feature.screeneffect.effect.renderer.FocusedChromaticAberrationEffectRenderer;
import dev.latvian.mods.vidlib.feature.session.ClientLevelSessionData;
import dev.latvian.mods.vidlib.feature.session.ClientPlayerSessionData;
import dev.latvian.mods.vidlib.feature.session.ClientSessionData;
import dev.latvian.mods.vidlib.feature.visual.Visuals;
import dev.latvian.mods.vidlib.feature.zone.Zone;
import dev.latvian.mods.vidlib.util.NameDrawType;
import dev.mrbeastgaming.mods.hub.api.HubClientSession;
import dev.mrbeastgaming.mods.hub.api.data.HubFileType;
import dev.mrbeastgaming.mods.hub.api.data.HubUserCapabilities;
import dev.mrbeastgaming.mods.hub.api.gateway.HubClientGateway;
import dev.mrbeastgaming.mods.hub.client.LinkHubUserScreen;
import dev.mrbeastgaming.mods.hub.client.LinkMinecraftScreen;
import dev.mrbeastgaming.mods.hub.event.SyncClientFilesHubEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoader;
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
import net.neoforged.neoforge.client.event.RegisterDebugEntriesEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleGroupsEvent;
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

@EventBusSubscriber(modid = VidLib.ID, value = Dist.CLIENT)
public class VidLibClientEventHandler {
	public static boolean clientLoaded = false;

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(VidLibClientEventHandler::syncSetup);
	}

	public static void syncSetup() {
		Zone.REGISTRY.addUpdateListener((values, type) -> {
			if (type == CustomRegistryUpdateCallback.Type.REMOTE) {
				ClientSessionData.of(Minecraft.getInstance()).updateZones(values);
			}
		});

		Atmosphere.REGISTRY.addUpdateListener((values, type) -> {
			if (type == CustomRegistryUpdateCallback.Type.REMOTE || type == CustomRegistryUpdateCallback.Type.CLIENT) {
				var session = ClientSessionData.of(Minecraft.getInstance());

				if (session != null) {
					session.updateAtmospheres(values);
				}
			}
		});

		ModLoader.postEvent(new ScreenEffectRenderingRegistryEvent());
	}

	@SubscribeEvent
	public static void dynamicResources(DynamicResourceEvent.Assets event) {
		event.register(ID.vidlib("dynamic_resources/clothing"));
	}

	@SubscribeEvent
	public static void addReloadListeners(AddClientReloadListenersEvent event) {
		// event.addListener(ID.vidlib("structure"), new StructureStorage(StructureStorage.CLIENT));
		// event.addListener(ID.vidlib("ghost_structure"), new GhostStructure.ClientLoader());
		event.addListener(ID.vidlib("clothing_preset"), new ClothingPresetLoader());
		event.addListener(ID.vidlib("physics_particle_data"), new PhysicsParticleData.Loader(RefType.CLIENT));
		event.addListener(ID.vidlib("atmosphere"), new Atmosphere.Loader(RefType.CLIENT));

		event.addDependency(ID.klib("gradient"), ID.vidlib("clothing_preset"));
		// event.addDependency(ID.vidlib("structure"), ID.vidlib("ghost_structure"));
	}

	@SubscribeEvent
	public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
		VidLibClientParticles.registerParticleProviders(event);
	}

	@SubscribeEvent
	public static void registerParticleGroups(RegisterParticleGroupsEvent event) {
		VidLibClientParticles.registerParticleGroups(event);
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
	public static void propRenderStateRegistry(PropRenderStateRegistryEvent event) {
		event.register(DangerProp.TYPE, DangerPropRenderState::new);
		event.register(DecalProp.TYPE, DecalPropRenderState::new);
		event.register(ImageProp.TYPE, ImagePropRenderState::new);
		event.register(ShapeProp.TYPE, ShapePropRenderState::new);
		event.register(TextProp.TYPE, TextPropRenderState::new);
		event.register(TVProp.TYPE, TVPropRenderState::new);
		event.register(MannequinProp.TYPE, MannequinPropRenderState::new);
	}

	@SubscribeEvent
	public static void screenEffectRenderingRegistry(ScreenEffectRenderingRegistryEvent event) {
		event.register(FocusedChromaticAberrationEffect.TYPE, FocusedChromaticAberrationEffectRenderer::build);
		event.register(AngledChromaticAberrationEffect.TYPE, AngledChromaticAberrationEffectRenderer::build);
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

		Visuals.TICK.clear();
		ScreenText.CLIENT_TICK.clear();
		var mc = Minecraft.getInstance();

		if (mc.level != null && mc.player != null) {
			var session = ClientSessionData.of(mc);

			if (VidLibClient.levelTick == Integer.MAX_VALUE) {
				VidLibClient.levelTick = 0;
			} else {
				VidLibClient.levelTick++;
			}

			ScreenText.CLIENT_TICK.ops = session.jsonOps;

			var tool = VidLibTool.of(mc.player);

			if (tool != null) {
				// tool.getSecond().debugText(mc.player, tool.getFirst(), mc.hitResult, ScreenText.CLIENT_TICK);
			}

			session.preTick(session.getPauseType());
		}

		VidLibKeys.handle(mc);
	}

	@SubscribeEvent
	public static void clientPostTick(ClientTickEvent.Post event) {
		var mc = Minecraft.getInstance();

		if (mc.player != null) {
			var session = ClientSessionData.of(mc);

			if (session != null) {
				session.postTick(session.getPauseType());
			}
		}
	}

	@SubscribeEvent
	public static void keyInput(InputEvent.Key event) {
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void preFrameGraphSetup(FrameGraphSetupEvent event) {
		var mc = Minecraft.getInstance();
		var player = mc.player;

		if (player == null || mc.level == null) {
			return;
		}

		var session = ClientSessionData.of(mc);
		session.projectedCoordinates = ProjectedCoordinates.of(mc, event.getCameraState());

		if (mc.options.getCameraType() == CameraType.FIRST_PERSON && VidLibClientOptions.getShowZones()) {
			var rayLine = mc.gameRenderer.mainCamera().klib$ray(512D);
			session.zoneHit = ClientLevelSessionData.of(mc.level).zones.hit(new HitBuilder(rayLine.start(), rayLine.end()));
		} else {
			session.zoneHit = null;
		}

		var tool = VidLibTool.of(player);

		var screenDelta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);

		if (tool != null) {
			tool.getSecond().renderSetup(player, tool.getFirst(), mc.hitResult, screenDelta);
		}
	}

	@SubscribeEvent
	public static void extract(ExtractLevelRenderStateEvent event) {
		var state = new VLWorldRenderState();
		state.extract(event);
		event.getRenderState().setRenderData(VidLibEntityRenderStates.WORLD_KEY, state);
	}

	@SubscribeEvent
	public static void submit(SubmitCustomGeometryEvent event) {
		var state = VLWorldRenderState.of(event.getLevelRenderState());
		state.submit(event);
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
		var session = ClientSessionData.of(mc);
		var playerSession = ClientPlayerSessionData.of(mc.player);
		var graphics = event.getGuiGraphics();
		var delta = event.getPartialTick().getGameTimeDeltaPartialTick(true);
		int width = event.getGuiGraphics().guiWidth();
		int height = event.getGuiGraphics().guiHeight();

		/*
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
		 */

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

		if (!ClientGameEngine.INSTANCE.hideGui(mc) && !session.isReplay) {
			ScreenText.RENDER.addAll(ScreenText.CLIENT_TICK);
			ScreenText.RENDER.ops = session.jsonOps;

			if (mc.gui.screen() == null || mc.gui.screen() instanceof ChatScreen) {
				NeoForge.EVENT_BUS.post(new DebugTextEvent.Render(ScreenText.RENDER));

				var zoneHit = session.zoneHit;

				if (zoneHit != null) {
					var component = Component.literal("Zone: ").append(Component.literal(zoneHit.zone().key()).withStyle(ChatFormatting.AQUA));

					if (zoneHit.zone().value().generated()) {
						component.append(Component.literal("*").withStyle(ChatFormatting.LIGHT_PURPLE));
					}

					ScreenText.RENDER.topLeft.add(component);

					var zoneTag = zoneHit.resolvedZone().data;

					if (!zoneTag.isEmpty()) {
						for (var key : zoneTag.keySet()) {
							ScreenText.RENDER.topLeft.add(Component.literal(key + ": ").append(NbtUtils.toPrettyComponent(zoneTag.get(key))));
						}
					}
				}

				if (!playerSession.zoneTagsIn.isEmpty() && VidLibClientOptions.getShowZones()) {
					ScreenText.RENDER.topRight.add("Zones in:");

					for (var tag : playerSession.zoneTagsIn) {
						ScreenText.RENDER.topRight.add(tag.toString());
					}
				}

				graphics.pose().translate(0F, 0F);

				if (mc.gui.screen() instanceof ChatScreen) {
					ScreenTextRenderer.render(graphics, ScreenText.RENDER, mc.font, 0, 0, width, height - 14, 0x40000000, 0x70FFFFFF);
				} else {
					ScreenTextRenderer.render(graphics, ScreenText.RENDER, mc.font, 0, 0, width, height, 0xA0000000, 0xFFFFFFFF);
				}
			}
		}
	}

	@SubscribeEvent
	public static void adjustFOV(ViewportEvent.ComputeFov event) {
		var mc = Minecraft.getInstance();
		var override = ClientGameEngine.INSTANCE.overrideCamera(mc);

		if (override != null) {
			event.setFOV(event.getFOV() * override.getFOVModifier(event.getPartialTick()));
		}

		/*
		if (mc.player != null && mc.player.getVehicle() != null) {
			event.setFOV(event.getFOV() * mc.player.getVehicle().adjustFOV(camera, event.getPartialTick(), changingFov));
		}
		 */
	}

	@SubscribeEvent
	public static void registerDebugEntries(RegisterDebugEntriesEvent event) {
		event.register(ID.vidlib("physics_particles"), PhysicsParticleManager::debugInfo);
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
			if (CommonGameEngine.INSTANCE.isSuspended(mc.player)) {
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

		if (!ClientSessionData.of(Minecraft.getInstance()).clientModListSentDuringConfig) {
			event.getPlayer().c2s(new ClientModListPayload(PlatformHelper.INSTANCE.getModList()));
		}
	}

	@SubscribeEvent
	public static void loggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
		AutoInit.Type.ASSETS_CLOSED.invoke();
	}

	@SubscribeEvent
	public static void clientResourceLoadFinished(ClientResourceLoadFinishedEvent event) {
		var mc = Minecraft.getInstance();
		AutoInit.Type.ASSETS_LOADED.invoke(mc.getResourceManager());
	}

	@SubscribeEvent
	public static void canRenderNameTag(RenderNameTagEvent.CanRender event) {
		var mc = Minecraft.getInstance();

		if (event.getEntity() instanceof Player && ClientSessionData.of(mc).getNameDrawType() != NameDrawType.VANILLA) {
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
			if (!PlatformHelper.INSTANCE.isDevEnv()) {
				LinkMinecraftScreen.handle(event.getMinecraft(), true);
			}
		}
	}

	@SubscribeEvent
	public static void gameShuttingDown(GameShuttingDownEvent event) {
		VidLib.LOGGER.info("Shutting down");
		HubClientGateway.stopGateway();

		var thread = new Thread(() -> {
			try {
				Thread.sleep(30_000L);
			} catch (InterruptedException ignored) {
				return;
			}

			VidLib.LOGGER.warn("Game process did not terminate after shutdown, forcing exit");
			System.exit(0);
		}, "VidLib-Exit-SafetyNet");

		thread.setDaemon(true);
		thread.start();
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
