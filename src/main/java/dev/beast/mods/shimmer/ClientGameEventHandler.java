package dev.beast.mods.shimmer;

import com.mojang.math.Axis;
import com.mojang.serialization.JsonOps;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ClientCommandHolder;
import dev.beast.mods.shimmer.feature.clock.ClockRenderer;
import dev.beast.mods.shimmer.feature.cutscene.ClientCutscene;
import dev.beast.mods.shimmer.feature.icon.renderer.IconRenderer;
import dev.beast.mods.shimmer.feature.item.ShimmerTool;
import dev.beast.mods.shimmer.feature.misc.CameraOverride;
import dev.beast.mods.shimmer.feature.misc.DebugText;
import dev.beast.mods.shimmer.feature.misc.DebugTextEvent;
import dev.beast.mods.shimmer.feature.misc.InternalPlayerData;
import dev.beast.mods.shimmer.feature.misc.MiscShimmerClientUtils;
import dev.beast.mods.shimmer.feature.particle.physics.PhysicsParticleManager;
import dev.beast.mods.shimmer.feature.particle.physics.PhysicsParticleRenderContext;
import dev.beast.mods.shimmer.feature.structure.GhostStructure;
import dev.beast.mods.shimmer.feature.zone.renderer.ZoneRenderer;
import dev.beast.mods.shimmer.math.KMath;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ToastAddEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientGameEventHandler {
	public static boolean clientLoaded = false;

	@SubscribeEvent
	public static void clientPreTick(ClientTickEvent.Pre event) {
		GameEventHandler.gameLoaded();

		if (!clientLoaded) {
			clientLoaded = true;
			AutoInit.Type.CLIENT_LOADED.invoke();
		}

		DebugText.CLIENT_TICK.clear();
		var mc = Minecraft.getInstance();

		if (mc.level != null) {
			DebugText.CLIENT_TICK.ops = mc.level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
		} else {
			DebugText.CLIENT_TICK.ops = JsonOps.INSTANCE;
		}

		mc.shimmer$preTick(mc.getPauseType());
	}

	@SubscribeEvent
	public static void clientPostTick(ClientTickEvent.Post event) {
		var mc = Minecraft.getInstance();
		mc.shimmer$postTick(mc.getPauseType());
		NeoForge.EVENT_BUS.post(new DebugTextEvent.ClientTick(DebugText.CLIENT_TICK));
	}

	@SubscribeEvent
	public static void renderWorld(RenderLevelStageEvent event) {
		var mc = Minecraft.getInstance();

		if (mc.level == null || mc.player == null) {
			return;
		}

		var session = mc.player.shimmer$sessionData();
		float delta = event.getPartialTick().getGameTimeDeltaPartialTick(true);

		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
			mc.shimmer$renderSetup(event, delta);
		} else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
			var ms = event.getPoseStack();
			var cameraPos = event.getCamera().getPosition();
			var frustum = event.getFrustum();

			if (mc.player.get(InternalPlayerData.SHOW_ZONES)) {
				ZoneRenderer.renderAll(mc, session, delta, ms, cameraPos, frustum);
			}

			for (var instance : session.clocks.values()) {
				if (instance.clock.dimension() == mc.level.dimension()) {
					for (var location : instance.clock.locations()) {
						ClockRenderer.render(mc, instance, location, ms, cameraPos, delta);
					}
				}
			}

			if (!GhostStructure.LIST.isEmpty()) {
				for (var gs : GhostStructure.LIST) {
					if (gs.visibleTo().test(mc.player)) {
						ms.pushPose();
						ms.translate(gs.pos().x - cameraPos.x, gs.pos().y - cameraPos.y, gs.pos().z - cameraPos.z);
						ms.scale((float) gs.scale().x, (float) gs.scale().y, (float) gs.scale().z);
						ms.mulPose(Axis.YP.rotationDegrees((float) gs.rotation().y));
						ms.mulPose(Axis.XP.rotationDegrees((float) gs.rotation().x));
						ms.mulPose(Axis.ZP.rotationDegrees((float) gs.rotation().z));
						gs.structure().render(ms);
						ms.popPose();
					}
				}
			}

			var cc = ClientCutscene.instance;

			if (cc != null) {
				for (var task : cc.steps) {
					if (task.render != null && cc.totalTick >= task.start && cc.totalTick <= task.start + task.length) {
						float tick = Math.max(cc.totalTick - 1 + delta, 0F);
						var progress = KMath.clamp((tick - task.start) / (float) task.length, 0F, 1F);

						if (progress < 1F) {
							var target = task.prevRenderTarget == null || task.renderTarget == null ? cc.prevTarget.lerp(cc.target, delta) : task.prevRenderTarget.lerp(task.renderTarget, delta);

							for (var render : task.render) {
								render.render(mc, event, delta, progress, target);
							}
						}
					}
				}
			}

			for (var player : mc.level.players()) {
				if (player.isInvisible()) {
					continue;
				}

				var h = player.shimmer$sessionData().plumbobIcon;

				if (h == null) {
					continue;
				}

				var source = mc.renderBuffers().bufferSource();
				var blockpos = BlockPos.containing(player.getLightProbePosition(delta));
				int light = LightTexture.pack(mc.level.getBrightness(LightLayer.BLOCK, blockpos), mc.level.getBrightness(LightLayer.SKY, blockpos));

				var cam = mc.gameRenderer.getMainCamera().getPosition();
				var pos = player.getPosition(delta);

				if (KMath.sq(pos.x - cam.x) + KMath.sq(pos.z - cam.z) <= 0.01D * 0.01D) {
					continue;
				}

				ms.pushPose();
				ms.translate(pos.x - cameraPos.x, pos.y - cameraPos.y, pos.z - cameraPos.z);
				ms.translate(0F, player.isCrouching() ? 2.3F : 2.6F, 0F);
				ms.mulPose(mc.gameRenderer.getMainCamera().rotation());
				ms.scale(0.4F, 0.4F, 0.4F);

				if (h.renderer == null) {
					h.renderer = IconRenderer.create(h.icon);
				}

				((IconRenderer) h.renderer).render3D(mc, ms, delta, source, light, OverlayTexture.NO_OVERLAY);
				ms.popPose();
			}
		} else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
			PhysicsParticleManager.renderAll(new PhysicsParticleRenderContext(
				mc,
				!mc.player.isReplayCamera(),
				event.getPoseStack(),
				event.getProjectionMatrix(),
				delta,
				event.getCamera(),
				event.getFrustum(),
				ShimmerConfig.physicsParticleRenderLOD * ShimmerConfig.physicsParticleRenderLOD
			));
		}
	}

	@SubscribeEvent
	public static void renderHUD(RenderGuiEvent.Post event) {
		var mc = Minecraft.getInstance();

		if (mc.level == null || mc.player == null || mc.options.hideGui) {
			return;
		}

		DebugText.RENDER.ops = mc.level.registryAccess().createSerializationContext(JsonOps.INSTANCE);

		var session = mc.player.shimmer$sessionData();

		var graphics = event.getGuiGraphics();

		if ((mc.isLocalServer() || mc.player.hasPermissions(2)) && (mc.screen == null || mc.screen instanceof ChatScreen)) {
			var tool = ShimmerTool.of(mc.player);

			if (tool != null) {
				tool.getSecond().debugText(mc.player, tool.getFirst(), mc.hitResult, DebugText.RENDER);
			}

			NeoForge.EVENT_BUS.post(new DebugTextEvent.Render(DebugText.RENDER));

			var zoneClip = session.zoneClip;

			if (zoneClip != null) {
				var component = Component.literal("Zone: ").append(Component.literal(zoneClip.instance().container.id.toString()).withStyle(ChatFormatting.AQUA));

				if (zoneClip.instance().container.zones.size() > 1) {
					component.append(Component.literal("[" + zoneClip.instance().index + "]").withStyle(ChatFormatting.GREEN));
				}

				DebugText.RENDER.topLeft.add(component);

				var zoneTag = zoneClip.instance().zone.data();

				if (!zoneTag.isEmpty()) {
					for (var key : zoneTag.getAllKeys()) {
						DebugText.RENDER.topLeft.add(Component.literal(key + ": ").append(NbtUtils.toPrettyComponent(zoneTag.get(key))));
					}
				}
			}

			if (!session.zonesTagsIn.isEmpty()) {
				DebugText.RENDER.topRight.add("Zones in:");

				for (var tag : session.zonesTagsIn) {
					DebugText.RENDER.topRight.add(tag);
				}
			}

			graphics.pose().pushPose();
			graphics.pose().translate(0F, 0F, 800F);

			for (int i = 0; i < DebugText.RENDER.topLeft.list.size(); i++) {
				int w = mc.font.width(DebugText.RENDER.topLeft.list.get(i));
				int x = 1;
				int y = 2 + i * 11;
				graphics.fill(x, y, x + w + 3, y + 11, 0xA0000000);
				graphics.drawString(mc.font, DebugText.RENDER.topLeft.list.get(i), x + 2, y + 2, 0xFFFFFFFF, true);
			}

			for (int i = 0; i < DebugText.RENDER.topRight.list.size(); i++) {
				int w = mc.font.width(DebugText.RENDER.topRight.list.get(i));
				int x = event.getGuiGraphics().guiWidth() - w - 4;
				int y = 2 + i * 11;
				graphics.fill(x, y, x + w + 3, y + 11, 0xA0000000);
				graphics.drawString(mc.font, DebugText.RENDER.topRight.list.get(i), x + 2, y + 2, 0xFFFFFFFF, true);
			}

			for (int i = 0; i < DebugText.RENDER.bottomLeft.list.size(); i++) {
				int w = mc.font.width(DebugText.RENDER.bottomLeft.list.get(i));
				int x = 1;
				int y = i * 11 + event.getGuiGraphics().guiHeight() - DebugText.RENDER.bottomLeft.list.size() * 11 - 2;
				graphics.fill(x, y, x + w + 3, y + 11, 0xA0000000);
				graphics.drawString(mc.font, DebugText.RENDER.bottomLeft.list.get(i), x + 2, y + 2, 0xFFFFFFFF, true);
			}

			for (int i = 0; i < DebugText.RENDER.bottomRight.list.size(); i++) {
				int w = mc.font.width(DebugText.RENDER.bottomRight.list.get(i));
				int x = event.getGuiGraphics().guiWidth() - w - 4;
				int y = i * 11 + event.getGuiGraphics().guiHeight() - DebugText.RENDER.bottomRight.list.size() * 11 - 2;
				graphics.fill(x, y, x + w + 3, y + 11, 0xA0000000);
				graphics.drawString(mc.font, DebugText.RENDER.bottomRight.list.get(i), x + 2, y + 2, 0xFFFFFFFF, true);
			}

			graphics.pose().popPose();
		}

		DebugText.RENDER.clear();
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

		if (mc.player != null && mc.level != null && !mc.player.isSpectatorOrCreative() && mc.level.getBlockState(event.getTarget().getBlockPos()).is(Blocks.BARRIER)) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void debugText(CustomizeGuiOverlayEvent.DebugText event) {
		var mc = Minecraft.getInstance();

		// event.getLeft().clear();
		// event.getRight().clear();
		// event.getLeft().add(mc.fpsString);
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
}
