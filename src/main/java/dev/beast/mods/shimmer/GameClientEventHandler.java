package dev.beast.mods.shimmer;

import com.mojang.serialization.JsonOps;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ClientCommandHolder;
import dev.beast.mods.shimmer.feature.clock.Clock;
import dev.beast.mods.shimmer.feature.clock.ClockRenderer;
import dev.beast.mods.shimmer.feature.cutscene.ClientCutscene;
import dev.beast.mods.shimmer.feature.data.InternalServerData;
import dev.beast.mods.shimmer.feature.icon.PlumbobRenderer;
import dev.beast.mods.shimmer.feature.item.ShimmerTool;
import dev.beast.mods.shimmer.feature.misc.CameraOverride;
import dev.beast.mods.shimmer.feature.misc.DebugTextEvent;
import dev.beast.mods.shimmer.feature.misc.MiscShimmerClientUtils;
import dev.beast.mods.shimmer.feature.misc.ScreenText;
import dev.beast.mods.shimmer.feature.misc.ShimmerIcon;
import dev.beast.mods.shimmer.feature.particle.physics.PhysicsParticleManager;
import dev.beast.mods.shimmer.feature.structure.GhostStructure;
import dev.beast.mods.shimmer.feature.structure.StructureRenderer;
import dev.beast.mods.shimmer.feature.zone.renderer.ZoneRenderer;
import dev.beast.mods.shimmer.util.FrameInfo;
import dev.beast.mods.shimmer.util.JsonUtils;
import dev.latvian.mods.kmath.KMath;
import dev.latvian.mods.kmath.render.BoxRenderer;
import dev.latvian.mods.kmath.render.DebugRenderTypes;
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
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ToastAddEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.nio.file.Files;
import java.util.List;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class GameClientEventHandler {
	public static boolean clientLoaded = false;

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

		var tool = ShimmerTool.of(mc.player);

		if (tool != null) {
			tool.getSecond().debugText(mc.player, tool.getFirst(), mc.hitResult, ScreenText.CLIENT_TICK);
		}

		for (var clock : Clock.REGISTRY) {
			if (clock.screen().isPresent()) {
				var screen = clock.screen().get();
				var value = mc.player.shimmer$sessionData().clocks.get(clock.id());

				if (value != null && screen.visible().test(mc.player)) {
					var string = screen.format().formatted(value.second() / 60, value.second() % 60);
					var color = screen.color().lerp(switch (value.type()) {
						case FINISHED -> 1F;
						case FLASH -> 0.65F + Mth.cos((mc.player.shimmer$sessionData().tick) * 0.85F) * 0.35F;
						default -> 0F;
					}, Clock.RED);

					ScreenText.CLIENT_TICK.get(screen.location()).add(Component.literal(string).withStyle(Style.EMPTY.withColor(color.rgb())));
				}
			}
		}

		mc.shimmer$preTick(mc.getPauseType());

		while (mc.level != null && MiscShimmerClientUtils.freezeTickKeyMapping.consumeClick()) {
			if (!mc.player.isReplayCamera()) {
				if (mc.level.tickRateManager().isFrozen()) {
					mc.player.connection.sendCommand("tick unfreeze");
				} else {
					mc.player.connection.sendCommand("tick freeze");
				}
			}
		}

		while (MiscShimmerClientUtils.clearParticlesKeyMapping.consumeClick()) {
			mc.level.removeAllParticles();
		}

		while (MiscShimmerClientUtils.tpNKeyMapping.consumeClick()) {
			mc.player.connection.sendCommand("tp ~ ~ ~-10000");
		}

		while (MiscShimmerClientUtils.tpSKeyMapping.consumeClick()) {
			mc.player.connection.sendCommand("tp ~ ~ ~10000");
		}

		while (MiscShimmerClientUtils.tpWKeyMapping.consumeClick()) {
			mc.player.connection.sendCommand("tp ~-10000 ~ ~");
		}

		while (MiscShimmerClientUtils.tpEKeyMapping.consumeClick()) {
			mc.player.connection.sendCommand("tp ~10000 ~ ~");
		}
	}

	@SubscribeEvent
	public static void clientPostTick(ClientTickEvent.Post event) {
		var mc = Minecraft.getInstance();
		mc.shimmer$postTick(mc.getPauseType());
		NeoForge.EVENT_BUS.post(new DebugTextEvent.ClientTick(ScreenText.CLIENT_TICK));

		if (!MiscShimmerClientUtils.CLIENT_CLOSEABLE.isEmpty()) {
			for (var c : MiscShimmerClientUtils.CLIENT_CLOSEABLE) {
				try {
					c.close();
				} catch (Exception ignored) {
				}
			}

			MiscShimmerClientUtils.CLIENT_CLOSEABLE.clear();
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void renderWorld(RenderLevelStageEvent event) {
		var mc = Minecraft.getInstance();

		if (mc.level == null || mc.player == null) {
			return;
		}

		var session = mc.player.shimmer$sessionData();
		var frame = new FrameInfo(mc, session, event);
		session.currentFrameInfo = frame;
		session.worldMouse = null;
		float delta = frame.worldDelta();

		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
			mc.shimmer$renderSetup(frame);
		} else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
			var ms = frame.poseStack();

			if (mc.player.getShowZones()) {
				ZoneRenderer.renderAll(frame);
			} else if (!session.filteredZones.getSolidZones().isEmpty()) {
				ZoneRenderer.renderSolid(frame);
			}

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

			GhostStructure.render(frame);

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

			var tool = ShimmerTool.of(mc.player);

			if (tool != null) {
				var visuals = tool.getSecond().visuals(mc.player, tool.getFirst(), frame.screenDelta());

				for (var cube : visuals.cubes()) {
					BoxRenderer.renderVoxelShape(ms, frame.buffers(), cube.shape(), cube.pos().subtract(frame.camera().getPosition()), false, cube.color().withAlpha(50), cube.lineColor());
				}

				for (var line : visuals.lines()) {
					var rx = frame.x(line.line().start().x);
					var ry = frame.y(line.line().start().y);
					var rz = frame.z(line.line().start().z);

					var m = ms.last().pose();
					var buffer = frame.buffers().getBuffer(DebugRenderTypes.LINES);
					buffer.addVertex(m, rx, ry, rz).setColor(line.startColor().argb());
					buffer.addVertex(m, rx + (float) line.line().dx(), ry + (float) line.line().dy(), rz + (float) line.line().dz()).setColor(line.endColor().argb());
				}
			}
		} else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
			PhysicsParticleManager.renderAll(frame);
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

		var session = mc.player.shimmer$sessionData();
		var graphics = event.getGuiGraphics();
		var delta = event.getPartialTick().getGameTimeDeltaPartialTick(true);
		int width = event.getGuiGraphics().guiWidth();
		int height = event.getGuiGraphics().guiHeight();

		int renderingStructures = StructureRenderer.getRenderingAll();

		if (renderingStructures != 0) {
			var component = Component.empty().append(ShimmerIcon.ERROR.prefix()).append("Rendering " + renderingStructures + " structures...");

			if (mc.player.isReplayCamera()) {
				int x = 1;
				int y = 2;
				graphics.fill(x, y, x + mc.font.width(component) + 3, y + 11, 0xA0000000);
				graphics.drawString(mc.font, component, x + 2, y + 2, 0xFFFFFFFF, true);
			} else {
				ScreenText.RENDER.topLeft.add(component);
			}
		}

		if (!mc.options.hideGui && !mc.player.isReplayCamera()) {
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
						for (var key : zoneTag.getAllKeys()) {
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

				int textHeight = height;
				int bgColor = 0xA0000000;
				int color = 0xFFFFFFFF;

				if (mc.screen instanceof ChatScreen) {
					textHeight -= 14;
					bgColor = 0x40000000;
					color = 0x70FFFFFF;
				}

				for (int i = 0; i < ScreenText.RENDER.topLeft.list.size(); i++) {
					int w = mc.font.width(ScreenText.RENDER.topLeft.list.get(i));
					int x = 1;
					int y = 2 + i * 11;
					graphics.fill(x, y, x + w + 3, y + 11, bgColor);
					graphics.drawString(mc.font, ScreenText.RENDER.topLeft.list.get(i), x + 2, y + 2, color, true);
				}

				for (int i = 0; i < ScreenText.RENDER.topRight.list.size(); i++) {
					int w = mc.font.width(ScreenText.RENDER.topRight.list.get(i));
					int x = width - w - 4;
					int y = 2 + i * 11;
					graphics.fill(x, y, x + w + 3, y + 11, bgColor);
					graphics.drawString(mc.font, ScreenText.RENDER.topRight.list.get(i), x + 2, y + 2, color, true);
				}

				for (int i = 0; i < ScreenText.RENDER.bottomLeft.list.size(); i++) {
					int w = mc.font.width(ScreenText.RENDER.bottomLeft.list.get(i));
					int x = 1;
					int y = i * 11 + textHeight - ScreenText.RENDER.bottomLeft.list.size() * 11 - 2;
					graphics.fill(x, y, x + w + 3, y + 11, bgColor);
					graphics.drawString(mc.font, ScreenText.RENDER.bottomLeft.list.get(i), x + 2, y + 2, color, true);
				}

				for (int i = 0; i < ScreenText.RENDER.bottomRight.list.size(); i++) {
					int w = mc.font.width(ScreenText.RENDER.bottomRight.list.get(i));
					int x = width - w - 4;
					int y = i * 11 + textHeight - ScreenText.RENDER.bottomRight.list.size() * 11 - 2;
					graphics.fill(x, y, x + w + 3, y + 11, bgColor);
					graphics.drawString(mc.font, ScreenText.RENDER.bottomRight.list.get(i), x + 2, y + 2, color, true);
				}

				graphics.pose().popPose();
			}
		}

		if (session.screenFade != null) {
			float a = Math.clamp(Mth.lerp(delta, session.screenFade.prevAlpha, session.screenFade.alpha), 0F, 1F);

			if (a > 0F) {
				graphics.fill(0, 0, width, height, 1000, session.screenFade.color.withAlpha(a).argb());
			}
		}
	}

	@SubscribeEvent
	public static void adjustFOV(ViewportEvent.ComputeFov event) {
		var mc = Minecraft.getInstance();
		var override = CameraOverride.get(mc);

		if (override != null) {
			event.setFOV((float) (event.getFOV() * override.getZoom(event.getPartialTick())));
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

			var tool = ShimmerTool.of(mc.player);

			if (tool != null && tool.getSecond().visuals(mc.player, tool.getFirst(), event.getDeltaTracker().getGameTimeDeltaPartialTick(true)).contains(event.getTarget().getBlockPos())) {
				event.setCanceled(true);
			}
		}
	}

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
			var d = event.getRenderState().getRenderData(MiscShimmerClientUtils.CREATIVE);

			if (d != null && d) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void interactionKeyMappingTriggered(InputEvent.InteractionKeyMappingTriggered event) {
		var mc = Minecraft.getInstance();

		if (mc.player != null && event.isAttack()) {
			var item = mc.player.getItemInHand(event.getHand());
			var tool = ShimmerTool.of(item);

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
	public static void loggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
		var player = event.getPlayer();

		if (player != null) {
			var session = player.shimmer$sessionData();

			if (session.dataRecorder != null && session.dataRecorder.record) {
				try (var writer = Files.newBufferedWriter(FMLPaths.GAMEDIR.get().resolve("replay-data-" + Long.toUnsignedString(session.dataRecorder.start) + ".json"))) {
					JsonUtils.write(writer, session.dataRecorder.save(player.level().registryAccess().createSerializationContext(JsonOps.INSTANCE)), false);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}
