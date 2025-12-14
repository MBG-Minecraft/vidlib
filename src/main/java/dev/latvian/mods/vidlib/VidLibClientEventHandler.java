package dev.latvian.mods.vidlib;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.render.BufferSupplier;
import dev.latvian.mods.klib.render.CuboidRenderer;
import dev.latvian.mods.klib.texture.LightUV;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.auto.BlockEntityRendererHolder;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ClientCommandHolder;
import dev.latvian.mods.vidlib.feature.auto.EntityRendererHolder;
import dev.latvian.mods.vidlib.feature.bloom.Bloom;
import dev.latvian.mods.vidlib.feature.canvas.BossRendering;
import dev.latvian.mods.vidlib.feature.canvas.Canvas;
import dev.latvian.mods.vidlib.feature.client.ClientItemTooltips;
import dev.latvian.mods.vidlib.feature.client.VidLibClientOptions;
import dev.latvian.mods.vidlib.feature.client.VidLibEntityRenderStates;
import dev.latvian.mods.vidlib.feature.client.VidLibHUD;
import dev.latvian.mods.vidlib.feature.client.VidLibKeys;
import dev.latvian.mods.vidlib.feature.clock.Clock;
import dev.latvian.mods.vidlib.feature.clock.ClockFont;
import dev.latvian.mods.vidlib.feature.clock.ClockRenderer;
import dev.latvian.mods.vidlib.feature.clothing.ClientClothingLoader;
import dev.latvian.mods.vidlib.feature.clothing.ClothingLayer;
import dev.latvian.mods.vidlib.feature.clothing.ClothingModel;
import dev.latvian.mods.vidlib.feature.data.InternalServerData;
import dev.latvian.mods.vidlib.feature.dynamicresources.DynamicResourceEvent;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import dev.latvian.mods.vidlib.feature.environment.FluidPlaneRenderer;
import dev.latvian.mods.vidlib.feature.gradient.ClientGradientLoader;
import dev.latvian.mods.vidlib.feature.icon.PlumbobRenderer;
import dev.latvian.mods.vidlib.feature.item.VidLibTool;
import dev.latvian.mods.vidlib.feature.misc.CameraOverride;
import dev.latvian.mods.vidlib.feature.misc.ClientModInfo;
import dev.latvian.mods.vidlib.feature.misc.ClientModListPayload;
import dev.latvian.mods.vidlib.feature.misc.DebugTextEvent;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import dev.latvian.mods.vidlib.feature.misc.ScreenText;
import dev.latvian.mods.vidlib.feature.misc.ScreenTextRenderer;
import dev.latvian.mods.vidlib.feature.misc.VLFlashbackIntegration;
import dev.latvian.mods.vidlib.feature.misc.VidLibIcon;
import dev.latvian.mods.vidlib.feature.multiverse.VoidSpecialEffects;
import dev.latvian.mods.vidlib.feature.particle.VidLibClientParticles;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleData;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleManager;
import dev.latvian.mods.vidlib.feature.pin.Pins;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import dev.latvian.mods.vidlib.feature.prop.ClientProps;
import dev.latvian.mods.vidlib.feature.prop.PropHitResult;
import dev.latvian.mods.vidlib.feature.skybox.SkyboxData;
import dev.latvian.mods.vidlib.feature.structure.GhostStructure;
import dev.latvian.mods.vidlib.feature.structure.StructureCapture;
import dev.latvian.mods.vidlib.feature.structure.StructureRenderer;
import dev.latvian.mods.vidlib.feature.structure.StructureStorage;
import dev.latvian.mods.vidlib.feature.visual.TexturedCubeRenderer;
import dev.latvian.mods.vidlib.feature.visual.Visuals;
import dev.latvian.mods.vidlib.feature.waypoint.ClientWaypoints;
import dev.latvian.mods.vidlib.feature.zone.ZoneLoader;
import dev.latvian.mods.vidlib.feature.zone.renderer.ZoneRenderer;
import dev.latvian.mods.vidlib.integration.FlashbackIntegration;
import dev.latvian.mods.vidlib.util.NameDrawType;
import dev.latvian.mods.vidlib.util.StringUtils;
import dev.latvian.mods.vidlib.util.TerrainRenderLayer;
import dev.latvian.mods.vidlib.util.client.FrameInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.CalculateDetachedCameraDistanceEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientResourceLoadFinishedEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.FrameGraphSetupEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ToastAddEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
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
		if (VLFlashbackIntegration.ENABLED) {
			VLFlashbackIntegration.init();
		}
	}

	@SubscribeEvent
	public static void dynamicResources(DynamicResourceEvent.Assets event) {
		event.register(ID.video("dynamic_resources/tracksuits"));
	}

	@SubscribeEvent
	public static void addReloadListeners(AddClientReloadListenersEvent event) {
		event.addListener(VidLib.id("structure"), new StructureStorage(StructureStorage.CLIENT));
		event.addListener(VidLib.id("ghost_structure"), new GhostStructure.Loader());
		event.addListener(VidLib.id("clothing"), new ClientClothingLoader());
		event.addListener(VidLib.id("physics_particle_data"), new PhysicsParticleData.Loader());
		event.addListener(VidLib.id("gradient"), new ClientGradientLoader());
		event.addListener(VidLib.id("clock_font"), new ClockFont.Loader());
		event.addListener(VidLib.id("clock"), new Clock.Loader());
		event.addListener(VidLib.id("skybox"), new SkyboxData.Loader());
		event.addListener(VidLib.id("zone"), new ZoneLoader(ZoneLoader.CLIENT_BY_DIMENSION, false));

		event.addDependency(VidLib.id("structure"), VidLib.id("ghost_structure"));
		event.addDependency(VidLib.id("clock_font"), VidLib.id("clock"));
	}

	@SubscribeEvent
	public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
		VidLibClientParticles.register(event);
	}

	@SubscribeEvent
	public static void registerDimensionSpecialEffects(RegisterDimensionSpecialEffectsEvent event) {
		event.register(VidLib.id("void"), new VoidSpecialEffects());
	}

	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		for (var s : ClientAutoRegister.SCANNED.get()) {
			if (s.value() instanceof EntityRendererHolder<?> holder) {
				holder.register(event);
			} else if (s.value() instanceof BlockEntityRendererHolder<?> holder) {
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
		event.registerBelowAll(VidLib.id("player_names"), VidLibHUD::drawPlayerNames);
		event.registerAbove(VanillaGuiLayers.BOSS_OVERLAY, VidLib.id("above_boss"), VidLibHUD::drawAboveBossOverlay);
		event.registerAboveAll(VidLib.id("fade"), VidLibHUD::drawFade);
		event.registerAboveAll(VidLib.id("player_pins"), Pins::draw);
		event.registerBelowAll(VidLib.id("waypoints"), ClientWaypoints::draw);
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
			ScreenText.CLIENT_TICK.ops = mc.level.registryAccess().createSerializationContext(JsonOps.INSTANCE);

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
	}

	@SubscribeEvent
	public static void keyInput(InputEvent.Key event) {
		if (event.getAction() == GLFW.GLFW_PRESS && VidLibKeys.adminPanelKeyMapping.matches(event.getKey(), event.getScanCode()) && VidLibKeys.adminPanelKeyMapping.getKeyModifier().isActive(KeyConflictContext.UNIVERSAL)) {
			// var mc = Minecraft.getInstance();
			// boolean adminPanel = !VidLibClientOptions.getAdminPanel();
			// VidLibClientOptions.ADMIN_PANEL.set(adminPanel);
			// mc.options.save();
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void frameGraphSetup(FrameGraphSetupEvent event) {
		Minecraft.getInstance().vl$renderSetup(event);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void renderWorld(RenderLevelStageEvent event) {
		var mc = Minecraft.getInstance();

		if (mc.level == null || mc.player == null) {
			return;
		}

		var session = mc.player.vl$sessionData();
		var frame = new FrameInfo(mc, session, event);
		FrameInfo.CURRENT = frame;

		var ms = frame.poseStack();
		float delta = frame.worldDelta();

		if (session.fluidPlane != null && frame.layer() != null) {
			FluidPlaneRenderer.render(frame, session.fluidPlane);
		}

		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
			Canvas.MAIN_BEFORE_PARTICLES.copyColorFrom(mc.getMainRenderTarget());
			Canvas.MAIN_BEFORE_PARTICLES.copyDepthFrom(mc.getMainRenderTarget());
			BossRendering.render(frame);
		} else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
			Canvas.MAIN_AFTER_PARTICLES.copyColorFrom(mc.getMainRenderTarget());
			Canvas.MAIN_AFTER_PARTICLES.copyDepthFrom(mc.getMainRenderTarget());
		}

		if (frame.layer() == TerrainRenderLayer.CUTOUT) {
			if (ClockRenderer.VISIBLE.get()) {
				for (var clock : Clock.REGISTRY) {
					if (!clock.locations().isEmpty()) {
						var value = session.clocks.get(clock.id());

						if (value != null) {
							for (var location : clock.locations()) {
								if (location.dimension() == mc.level.dimension() && location.visible().test(mc.player)) {
									ClockRenderer.render(frame, value, location);
								}
							}
						}
					}
				}
			}
		} else if (frame.layer() == TerrainRenderLayer.PARTICLE) {
			if (VidLibClientOptions.getShowZones()) {
				ZoneRenderer.renderAll(frame);
			} else {
				ZoneRenderer.renderSolid(frame);
			}

			if (VidLibClientOptions.getShowAnchor()) {
				var areas = mc.getAnchor().shapes().get(mc.level.dimension());

				if (areas != null && !areas.isEmpty()) {
					var color = Color.YELLOW.withAlpha(80);
					var buffers = frame.buffers();
					var cull = true;

					for (var a : areas) {
						float minX = frame.x(a.minX() + 0.5D);
						float minY = frame.y(a.minY() + 0.5D);
						float minZ = frame.z(a.minZ() + 0.5D);
						float maxX = frame.x(a.maxX() + 0.5D);
						float maxY = frame.y(a.maxY() + 0.5D);
						float maxZ = frame.z(a.maxZ() + 0.5D);

						CuboidRenderer.frame(ms, minX, minY, minZ, maxX, maxY, maxZ, buffers, BufferSupplier.DEBUG_NO_DEPTH, cull, color, Color.YELLOW, 1F, 1F);
					}
				}
			}

			if (!session.serverDataMap.get(InternalServerData.HIDE_PLUMBOBS, mc.level.getGameTime())) {
				PlumbobRenderer.render(mc, frame);
			}

			var tool = VidLibTool.of(mc.player);

			if (tool != null) {
				tool.getSecond().visuals(mc.player, tool.getFirst(), Visuals.TEMP, frame.screenDelta());
				MiscClientUtils.renderVisuals(frame.poseStack(), frame.camera().getPosition(), frame.buffers(), BufferSupplier.DEBUG_NO_DEPTH, Visuals.TEMP, 1F);
				Visuals.TEMP.clear();
			}

			if (mc.getEntityRenderDispatcher().shouldRenderHitBoxes() || !ClientProps.OPEN_PROPS.isEmpty()) {
				mc.level.getProps().renderDebug(frame);
				ClientProps.OPEN_PROPS.clear();
			}

			Visuals.FRAME_DEBUG.copyFrom(Visuals.TICK_DEBUG);

			if (Visuals.FRAME_DEBUG.hasAny()) {
				MiscClientUtils.renderVisuals(frame.poseStack(), frame.camera().getPosition(), frame.buffers(), BufferSupplier.DEBUG_NO_DEPTH, Visuals.FRAME_DEBUG, 1F);
				Visuals.FRAME_DEBUG.clear();
			}
		}

		mc.level.getProps().renderAll(frame, ms);

		if (frame.layer() != null) {
			GhostStructure.render(frame);

			var tool = VidLibTool.of(mc.player);

			if (tool != null) {
				tool.getSecond().visuals(mc.player, tool.getFirst(), Visuals.TEMP, frame.screenDelta());

				for (var cube : Visuals.TEMP.texturedCubes()) {
					TexturedCubeRenderer.render(frame, LightUV.FULLBRIGHT, cube, Color.WHITE);
				}

				Visuals.TEMP.clear();
			}

			if (!VidLibClientOptions.getShowZones()) {
				ZoneRenderer.renderVisible(frame);
			}

			PhysicsParticleManager.render(frame);
		}

		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
			Bloom.CANVAS.copyDepthFrom(mc.getMainRenderTarget());
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
			if (!VLFlashbackIntegration.ENABLED || !FlashbackIntegration.isExporting()) {
				var component = Component.empty().append(VidLibIcon.ERROR.prefix()).append("Rendering " + renderingStructures + " structures...");

				if (mc.player.isReplayCamera()) {
					int x = 1;
					int y = 2;
					graphics.fill(x, y, x + mc.font.width(component) + 3, y + 11, 0xA0000000);
					graphics.drawString(mc.font, component, x + 2, y + 2, 0xFFFFFFFF, true);
				} else {
					ScreenText.RENDER.topLeft.add(component);
				}
			}
		}

		boolean primitiveF3Open = mc.gui.getDebugOverlay().showDebugScreen() && ClientGameEngine.INSTANCE.primitiveF3(mc);

		if (primitiveF3Open || VidLibClientOptions.getShowFPS()) {
			ScreenText.RENDER.topRight.add(mc.fpsString.split(" ", 2)[0] + " FPS");
		}

		if ((primitiveF3Open || VidLibClientOptions.getShowCoordinates()) && ClientGameEngine.INSTANCE.allowCoordinates(mc)) {
			var pos = mc.gameRenderer.getMainCamera().getPosition();
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
			ScreenText.RENDER.ops = mc.level.registryAccess().createSerializationContext(JsonOps.INSTANCE);

			if (mc.screen == null || mc.screen instanceof ChatScreen) {
				NeoForge.EVENT_BUS.post(new DebugTextEvent.Render(ScreenText.RENDER));

				var zoneClip = session.zoneClip;

				if (zoneClip != null) {
					var component = Component.literal("Zone: ").append(Component.literal(zoneClip.instance().container.id.toString()).withStyle(ChatFormatting.AQUA));

					if (zoneClip.instance().container.zones.size() > 1) {
						component.append(Component.literal("[" + zoneClip.instance().index + "]").withStyle(ChatFormatting.GREEN));
					}

					if (zoneClip.instance().container.isGenerated()) {
						component.append(Component.literal("*").withStyle(ChatFormatting.LIGHT_PURPLE));
					}

					ScreenText.RENDER.topLeft.add(component);

					var zoneTag = zoneClip.instance().zone.data();

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

				graphics.pose().pushPose();
				graphics.pose().translate(0F, 0F, 800F);

				if (mc.screen instanceof ChatScreen) {
					ScreenTextRenderer.render(graphics, ScreenText.RENDER, mc.font, 0, 0, width, height - 14, 0x40000000, 0x70FFFFFF);
				} else {
					ScreenTextRenderer.render(graphics, ScreenText.RENDER, mc.font, 0, 0, width, height, 0xA0000000, 0xFFFFFFFF);
				}

				graphics.pose().popPose();
			}
		}
	}

	@SubscribeEvent
	public static void adjustFOV(ViewportEvent.ComputeFov event) {
		var mc = Minecraft.getInstance();
		var override = CameraOverride.get(mc);

		if (override != null) {
			event.setFOV((float) (event.getFOV() * override.getFOVModifier(event.getPartialTick())));
		}

		/*
		if (mc.player != null && mc.player.getVehicle() != null) {
			event.setFOV(event.getFOV() * mc.player.getVehicle().adjustFOV(camera, event.getPartialTick(), changingFov));
		}
		 */
	}

	@SubscribeEvent
	public static void renderBlockHighlight(RenderHighlightEvent.Block event) {
		var mc = Minecraft.getInstance();

		if (mc.player != null && mc.level != null) {
			if (!mc.player.isSpectatorOrCreative() && mc.level.getBlockState(event.getTarget().getBlockPos()).is(Blocks.BARRIER)) {
				event.setCanceled(true);
				return;
			}

			var tool = VidLibTool.of(mc.player);

			if (tool != null) {
				tool.getSecond().visuals(mc.player, tool.getFirst(), Visuals.TEMP, event.getDeltaTracker().getGameTimeDeltaPartialTick(true));

				if (Visuals.TEMP.contains(event.getTarget().getBlockPos())) {
					event.setCanceled(true);
				}

				Visuals.TEMP.clear();
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void debugText(CustomizeGuiOverlayEvent.DebugText event) {
		var mc = Minecraft.getInstance();

		var left = event.getLeft();
		var right = event.getRight();

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

		if (toast instanceof TutorialToast || toast instanceof AdvancementToast || toast instanceof RecipeToast) {
			event.setCanceled(true);
		} else if (toast instanceof SystemToast systemToast) {
			var t = systemToast.getToken();

			if (t == SystemToast.SystemToastId.UNSECURE_SERVER_WARNING) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void renderPlayerPre(RenderPlayerEvent.Pre event) {
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
			var list = new ArrayList<ClientModInfo>();

			for (var mod : ModList.get().getMods()) {
				list.add(new ClientModInfo(mod.getModId(), mod.getDisplayName(), mod.getVersion().toString(), mod.getOwningFile().getFile().getFileName()));
			}

			event.getPlayer().c2s(new ClientModListPayload(list));
		}
	}

	@SubscribeEvent
	public static void loggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
	}

	@SubscribeEvent
	public static void clientResourceLoadFinished(ClientResourceLoadFinishedEvent event) {
		var mc = Minecraft.getInstance();
		mc.vl$clearProfileCache();
		AutoInit.Type.ASSETS_LOADED.invoke(mc.getResourceManager());
	}

	@SubscribeEvent
	public static void renderNameTag(RenderNameTagEvent.DoRender event) {
		if (BossRendering.active > 0) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void canRenderNameTag(RenderNameTagEvent.CanRender event) {
		var mc = Minecraft.getInstance();

		if (mc.player == null || !(event.getEntity() instanceof Player)) {
			return;
		}

		if (mc.getNameDrawType() != NameDrawType.VANILLA && VidLibHUD.shouldDrawName(mc, mc.player, (Player) event.getEntity())) {
			event.setCanRender(TriState.FALSE);
		}
	}

	@SubscribeEvent
	public static void mouseClicked(InputEvent.MouseButton.Pre event) {
		if (event.getAction() == InputConstants.PRESS) {
			var mc = Minecraft.getInstance();

			if (mc.player != null && mc.screen == null && mc.isWindowActive() && mc.hitResult instanceof PropHitResult hit && !ClientProps.isPropHidden(hit.prop)) {
				boolean cancel = hit.prop.onClientInteraction(mc.player, event.getButton(), hit.getLocation(), hit.getDirection());

				if (cancel) {
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(ClothingLayer.WIDE, ClothingModel::createWideClothingLayer);
		event.registerLayerDefinition(ClothingLayer.SLIM, ClothingModel::createSlimClothingLayer);
	}

	@SubscribeEvent
	public static void registerLayers(EntityRenderersEvent.AddLayers event) {
		if (event.getSkin(PlayerSkin.Model.WIDE) instanceof PlayerRenderer r) {
			r.addLayer(new ClothingLayer(r, event.getContext(), false));
		}

		if (event.getSkin(PlayerSkin.Model.SLIM) instanceof PlayerRenderer r) {
			r.addLayer(new ClothingLayer(r, event.getContext(), false)); // TODO: Fixme
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onItemTooltip(ItemTooltipEvent event) {
		ClientItemTooltips.onItemTooltip(event);
	}
}
