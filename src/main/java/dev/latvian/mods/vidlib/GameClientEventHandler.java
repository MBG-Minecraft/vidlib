package dev.latvian.mods.vidlib;

import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kmath.KMath;
import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.kmath.render.BufferSupplier;
import dev.latvian.mods.kmath.render.CuboidRenderer;
import dev.latvian.mods.kmath.render.DebugRenderTypes;
import dev.latvian.mods.kmath.texture.LightUV;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ClientCommandHolder;
import dev.latvian.mods.vidlib.feature.bloom.Bloom;
import dev.latvian.mods.vidlib.feature.canvas.BossRendering;
import dev.latvian.mods.vidlib.feature.canvas.Canvas;
import dev.latvian.mods.vidlib.feature.canvas.CanvasImpl;
import dev.latvian.mods.vidlib.feature.client.VidLibEntityRenderStates;
import dev.latvian.mods.vidlib.feature.client.VidLibKeys;
import dev.latvian.mods.vidlib.feature.clock.Clock;
import dev.latvian.mods.vidlib.feature.clock.ClockRenderer;
import dev.latvian.mods.vidlib.feature.cutscene.ClientCutscene;
import dev.latvian.mods.vidlib.feature.data.InternalServerData;
import dev.latvian.mods.vidlib.feature.icon.PlumbobRenderer;
import dev.latvian.mods.vidlib.feature.item.VidLibTool;
import dev.latvian.mods.vidlib.feature.misc.CameraOverride;
import dev.latvian.mods.vidlib.feature.misc.DebugTextEvent;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import dev.latvian.mods.vidlib.feature.misc.ScreenText;
import dev.latvian.mods.vidlib.feature.misc.ScreenTextRenderer;
import dev.latvian.mods.vidlib.feature.misc.VidLibIcon;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleManager;
import dev.latvian.mods.vidlib.feature.structure.GhostStructure;
import dev.latvian.mods.vidlib.feature.structure.GhostStructureCapture;
import dev.latvian.mods.vidlib.feature.structure.StructureRenderer;
import dev.latvian.mods.vidlib.feature.visual.TexturedCubeRenderer;
import dev.latvian.mods.vidlib.feature.zone.renderer.ZoneRenderer;
import dev.latvian.mods.vidlib.util.JsonUtils;
import dev.latvian.mods.vidlib.util.TerrainRenderLayer;
import dev.latvian.mods.vidlib.util.client.FrameInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.CalculateDetachedCameraDistanceEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientResourceLoadFinishedEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.FrameGraphSetupEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ToastAddEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.nio.file.Files;
import java.util.List;

@EventBusSubscriber(modid = VidLib.ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class GameClientEventHandler {
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
	public static void clientPreTick(ClientTickEvent.Pre event) {
		GameEventHandler.gameLoaded();

		if (!clientLoaded) {
			clientLoaded = true;
			AutoInit.Type.CLIENT_LOADED.invoke();
		}

		ScreenText.CLIENT_TICK.clear();
		var mc = Minecraft.getInstance();

		if (mc.level == null || mc.player == null) {
			return;
		}

		ScreenText.CLIENT_TICK.ops = mc.level.registryAccess().createSerializationContext(JsonOps.INSTANCE);

		var tool = VidLibTool.of(mc.player);

		if (tool != null) {
			tool.getSecond().debugText(mc.player, tool.getFirst(), mc.hitResult, ScreenText.CLIENT_TICK);
		}

		for (var clock : Clock.REGISTRY) {
			if (clock.screen().isPresent()) {
				var screen = clock.screen().get();
				var value = mc.player.vl$sessionData().clocks.get(clock.id());

				if (value != null && screen.visible().test(mc.player)) {
					var string = screen.format().formatted(value.second() / 60, value.second() % 60);
					var color = screen.color().lerp(switch (value.type()) {
						case FINISHED -> 1F;
						case FLASH -> 0.65F + Mth.cos((mc.player.vl$sessionData().tick) * 0.85F) * 0.35F;
						default -> 0F;
					}, Clock.RED);

					ScreenText.CLIENT_TICK.get(screen.location()).add(Component.literal(string).withStyle(Style.EMPTY.withColor(color.rgb())));
				}
			}
		}

		mc.vl$preTick(mc.getPauseType());

		VidLibKeys.handle(mc);
	}

	@SubscribeEvent
	public static void clientPostTick(ClientTickEvent.Post event) {
		var mc = Minecraft.getInstance();
		mc.vl$postTick(mc.getPauseType());
		NeoForge.EVENT_BUS.post(new DebugTextEvent.ClientTick(ScreenText.CLIENT_TICK));

		int b = GhostStructureCapture.CURRENT.getValue().blocks.size();

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

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void frameGraphSetup(FrameGraphSetupEvent event) {
		var mc = Minecraft.getInstance();
		var session = mc.player.vl$sessionData();
		FrameInfo.CURRENT = new FrameInfo(mc, session, event);

		mc.vl$renderSetup();

		var tool = VidLibTool.of(mc.player);

		var screenDelta = event.getDeltaTracker().getGameTimeDeltaPartialTick(true);

		if (tool != null) {
			tool.getSecond().renderSetup(mc.player, tool.getFirst(), mc.hitResult, screenDelta);
		}

		GhostStructure.preRender(FrameInfo.CURRENT, mc.level.globalContext());

		if (session.npcRecording != null) {
			session.npcRecording.record(System.currentTimeMillis(), screenDelta, mc.player);
		}

		CanvasImpl.createHandles(event.getFrameGrapBuilder(), event.getRenderTargetDescriptor());
		// event.enableOutlineProcessing();
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
		session.worldMouse = null;

		var ms = frame.poseStack();
		float delta = frame.worldDelta();

		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
			Bloom.CANVAS.copyDepthFrom(mc.getMainRenderTarget());
		} else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
			Canvas.MAIN_BEFORE_PARTICLES.copyColorFrom(mc.getMainRenderTarget());
			Canvas.MAIN_BEFORE_PARTICLES.copyDepthFrom(mc.getMainRenderTarget());
			BossRendering.render(frame);
		}

		if (frame.layer() == TerrainRenderLayer.CUTOUT) {
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
		} else if (frame.layer() == TerrainRenderLayer.PARTICLE) {
			if (mc.player.getShowZones()) {
				ZoneRenderer.renderAll(frame);
			} else {
				ZoneRenderer.renderSolid(frame);
			}

			if (mc.player.getShowAnchor()) {
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

			if (session.cameraOverride instanceof ClientCutscene cc) {
				for (var task : cc.steps) {
					int start = task.resolvedStart;
					int length = task.resolvedLength;

					if (task.render != null && cc.totalTick >= start && cc.totalTick <= start + length) {
						float tick = Math.max(cc.totalTick - 1 + delta, 0F);
						var progress = KMath.clamp((tick - start) / (float) length, 0F, 1F);

						if (progress < 1F) {
							var target = task.prevRenderTarget == null || task.renderTarget == null ? cc.prevTarget.lerp(cc.target, delta) : task.prevRenderTarget.lerp(task.renderTarget, delta);

							for (var render : task.render) {
								render.render(mc, frame, delta, progress, target);
							}
						}
					}
				}
			}

			if (!session.serverDataMap.get(InternalServerData.HIDE_PLUMBOBS, mc.level.getGameTime())) {
				PlumbobRenderer.render(mc, frame);
			}

			var variables = mc.level.getEnvironment().globalVariables();

			if (!session.terrainHighlights.isEmpty()) {
				var buffer = frame.buffers().getBuffer(DebugRenderTypes.QUADS_NO_CULL_NO_DEPTH).onlyPos();

				for (var th : session.terrainHighlights) {
					th.render(frame, buffer, variables);
				}
			}

			var tool = VidLibTool.of(mc.player);

			if (tool != null) {
				var visuals = tool.getSecond().visuals(mc.player, tool.getFirst(), frame.screenDelta());
				MiscClientUtils.renderVisuals(frame.poseStack(), frame.camera().getPosition(), frame.buffers(), BufferSupplier.DEBUG_NO_DEPTH, visuals, 1F);
			}

			if (mc.getEntityRenderDispatcher().shouldRenderHitBoxes()) {
				mc.level.getProps().renderDebug(frame);
			}
		}

		mc.level.getProps().renderAll(frame);

		if (frame.layer() != null) {
			GhostStructure.render(frame);

			var tool = VidLibTool.of(mc.player);

			if (tool != null) {
				var visuals = tool.getSecond().visuals(mc.player, tool.getFirst(), frame.screenDelta());

				for (var cube : visuals.texturedCubes()) {
					TexturedCubeRenderer.render(frame, LightUV.BRIGHT, cube, Color.WHITE);
				}
			}

			if (!mc.player.getShowZones()) {
				ZoneRenderer.renderVisible(frame);
			}

			PhysicsParticleManager.render(frame);
		}
	}

	@SubscribeEvent
	public static void renderHUD(RenderGuiEvent.Post event) {
		renderHUD0(event);
		ScreenText.RENDER.clear();
	}

	public static void renderHUD0(RenderGuiEvent.Post event) {
		var mc = Minecraft.getInstance();

		if (mc.level == null || mc.player == null) {
			return;
		}

		var session = mc.player.vl$sessionData();
		var graphics = event.getGuiGraphics();
		var delta = event.getPartialTick().getGameTimeDeltaPartialTick(true);
		int width = event.getGuiGraphics().guiWidth();
		int height = event.getGuiGraphics().guiHeight();

		int renderingStructures = StructureRenderer.getRenderingAll();

		if (renderingStructures != 0) {
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

		if (mc.player.getShowFPS()) {
			ScreenText.RENDER.topRight.add(mc.fpsString.split(" ", 2)[0] + " FPS");
		}

		if (!mc.vl$hideGui() && !mc.player.isReplayCamera()) {
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

					ScreenText.RENDER.topLeft.add(component);

					var zoneTag = zoneClip.instance().zone.data();

					if (!zoneTag.isEmpty()) {
						for (var key : zoneTag.keySet()) {
							ScreenText.RENDER.topLeft.add(Component.literal(key + ": ").append(NbtUtils.toPrettyComponent(zoneTag.get(key))));
						}
					}
				}

				if (!session.zonesTagsIn.isEmpty() && mc.player.getShowZones()) {
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

		if (session.screenFade != null) {
			float t = Mth.lerp(delta, session.screenFade.prevTick, session.screenFade.tick) / (float) session.screenFade.totalTicks;
			float a = Math.clamp(Mth.lerp(delta, session.screenFade.prevAlpha, session.screenFade.alpha), 0F, 1F);

			if (a > 0F) {
				graphics.fill(0, 0, width, height, 1000, session.screenFade.color.get(t).withAlpha(a).argb());
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

			if (tool != null && tool.getSecond().visuals(mc.player, tool.getFirst(), event.getDeltaTracker().getGameTimeDeltaPartialTick(true)).contains(event.getTarget().getBlockPos())) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void debugText(CustomizeGuiOverlayEvent.DebugText event) {
		var mc = Minecraft.getInstance();

		var left = event.getLeft();
		var right = event.getRight();

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
		for (var s : AutoRegister.SCANNED.get()) {
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
	public static void loggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
		var player = event.getPlayer();

		if (player != null) {
			var session = player.vl$sessionData();

			if (session.dataRecorder != null && session.dataRecorder.record) {
				try (var writer = Files.newBufferedWriter(FMLPaths.GAMEDIR.get().resolve("replay-data-" + Long.toUnsignedString(session.dataRecorder.start) + ".json"))) {
					JsonUtils.write(writer, session.dataRecorder.save(player.level().registryAccess().createSerializationContext(JsonOps.INSTANCE)), false);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
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
}
