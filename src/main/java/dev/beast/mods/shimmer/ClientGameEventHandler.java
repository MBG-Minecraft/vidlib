package dev.beast.mods.shimmer;

import com.mojang.math.Axis;
import dev.beast.mods.shimmer.feature.clock.ClockRenderer;
import dev.beast.mods.shimmer.feature.cutscene.ClientCutscene;
import dev.beast.mods.shimmer.feature.misc.CameraOverride;
import dev.beast.mods.shimmer.feature.misc.DebugText;
import dev.beast.mods.shimmer.feature.misc.DebugTextEvent;
import dev.beast.mods.shimmer.feature.misc.InternalPlayerData;
import dev.beast.mods.shimmer.feature.structure.GhostStructure;
import dev.beast.mods.shimmer.feature.toolitem.ToolItem;
import dev.beast.mods.shimmer.feature.zone.renderer.ZoneRenderer;
import dev.beast.mods.shimmer.math.KMath;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientGameEventHandler {
	@SubscribeEvent
	public static void clientPreTick(ClientTickEvent.Pre event) {
		DebugText.CLIENT_TICK.clear();
		Minecraft.getInstance().shimmer$preTick();
	}

	@SubscribeEvent
	public static void clientPostTick(ClientTickEvent.Post event) {
		Minecraft.getInstance().shimmer$postTick();
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
			Minecraft.getInstance().shimmer$renderSetup(event, delta);
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
		}
	}

	@SubscribeEvent
	public static void renderHUD(RenderGuiEvent.Post event) {
		var mc = Minecraft.getInstance();

		if (mc.level == null || mc.player == null || mc.options.hideGui) {
			return;
		}

		var graphics = event.getGuiGraphics();

		if (mc.isLocalServer() || mc.player.hasPermissions(2)) {
			var tool = ToolItem.of(mc.player);

			if (tool != null) {
				tool.getSecond().debugText(tool.getFirst(), mc.player, mc.hitResult, DebugText.RENDER);
			}

			NeoForge.EVENT_BUS.post(new DebugTextEvent.Render(DebugText.RENDER));

			var zoneClip = mc.player.shimmer$sessionData().zoneClip;

			if (zoneClip != null) {
				var component = Component.literal("Zone: ").append(Component.literal(zoneClip.instance().container.id.toString()).withStyle(ChatFormatting.AQUA));

				if (zoneClip.instance().container.zones.size() > 1) {
					component.append(Component.literal("[" + zoneClip.instance().index + "]").withStyle(ChatFormatting.GREEN));
				}

				DebugText.RENDER.topLeft.add(component);

				var zoneTag = zoneClip.instance().zone.data();

				if (!zoneTag.isEmpty()) {
					for (var key : zoneTag.getAllKeys()) {
						DebugText.RENDER.topRight.add(Component.literal(key + ": ").append(NbtUtils.toPrettyComponent(zoneTag.get(key))));
					}
				}
			}

			graphics.pose().pushPose();
			graphics.pose().translate(0F, 0F, 800F);

			for (int i = 0; i < DebugText.RENDER.topLeft.list.size(); i++) {
				int w = mc.font.width(DebugText.RENDER.topLeft.list.get(i));
				int x = 2;
				int y = 2 + i * 12;
				graphics.fill(x, y, x + w + 6, y + 12, 0xA0000000);
				graphics.drawString(mc.font, DebugText.RENDER.topLeft.list.get(i), x + 3, y + 2, 0xFFFFFFFF, true);
			}

			for (int i = 0; i < DebugText.RENDER.topRight.list.size(); i++) {
				int w = mc.font.width(DebugText.RENDER.topRight.list.get(i));
				int x = event.getGuiGraphics().guiWidth() - w - 8;
				int y = 2 + i * 12;
				graphics.fill(x, y, x + w + 6, y + 12, 0xA0000000);
				graphics.drawString(mc.font, DebugText.RENDER.topRight.list.get(i), x + 3, y + 2, 0xFFFFFFFF, true);
			}

			for (int i = 0; i < DebugText.RENDER.bottomLeft.list.size(); i++) {
				int w = mc.font.width(DebugText.RENDER.bottomLeft.list.get(i));
				int x = 2;
				int y = i * 12 + event.getGuiGraphics().guiHeight() - DebugText.RENDER.bottomLeft.list.size() * 12 - 2;
				graphics.fill(x, y, x + w + 6, y + 12, 0xA0000000);
				graphics.drawString(mc.font, DebugText.RENDER.bottomLeft.list.get(i), x + 3, y + 2, 0xFFFFFFFF, true);
			}

			for (int i = 0; i < DebugText.RENDER.bottomRight.list.size(); i++) {
				int w = mc.font.width(DebugText.RENDER.bottomRight.list.get(i));
				int x = event.getGuiGraphics().guiWidth() - w - 8;
				int y = i * 12 + event.getGuiGraphics().guiHeight() - DebugText.RENDER.bottomRight.list.size() * 12 - 2;
				graphics.fill(x, y, x + w + 6, y + 12, 0xA0000000);
				graphics.drawString(mc.font, DebugText.RENDER.bottomRight.list.get(i), x + 3, y + 2, 0xFFFFFFFF, true);
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
			event.setFOV(event.getFOV() * override.getZoom(event.getPartialTick()));
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
}
