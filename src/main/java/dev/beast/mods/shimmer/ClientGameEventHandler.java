package dev.beast.mods.shimmer;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.beast.mods.shimmer.content.clock.ClockBlockEntity;
import dev.beast.mods.shimmer.feature.cutscene.ClientCutscene;
import dev.beast.mods.shimmer.feature.zone.renderer.EmptyZoneRenderer;
import dev.beast.mods.shimmer.feature.zone.renderer.ZoneRenderer;
import dev.beast.mods.shimmer.math.Color;
import dev.beast.mods.shimmer.math.EasingGroup;
import dev.beast.mods.shimmer.math.KMath;
import dev.beast.mods.shimmer.util.Cast;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientGameEventHandler {
	@SubscribeEvent
	public static void clientPreTick(ClientTickEvent.Pre event) {
		var mc = Minecraft.getInstance();

		if (mc.level != null && mc.player != null) {
			ClockBlockEntity.tick();

			var session = mc.player.shimmer$sessionData();

			session.filteredZones.entityZones.clear();

			for (var container : session.filteredZones) {
				container.tick(session.filteredZones, mc.level);
			}
		}
	}

	@SubscribeEvent
	public static void clientPostTick(ClientTickEvent.Post event) {
		Minecraft.getInstance().shimmer$postTick();
	}

	@SubscribeEvent
	public static void renderWorld(RenderLevelStageEvent event) {
		var mc = Minecraft.getInstance();

		if (mc.level == null || mc.player == null) {
			return;
		}

		var session = mc.player.shimmer$sessionData();
		float delta = event.getPartialTick().getGameTimeDeltaPartialTick(true);

		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
			var start = mc.player.getEyePosition();
			var end = start.add(mc.player.getLookAngle().scale(500D));

			session.zoneClip = session.filteredZones.clip(start, end);
		} else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
			if (mc.getEntityRenderDispatcher().shouldRenderHitBoxes()) {
				for (var container : session.filteredZones) {
					for (var instance : container.zones) {
						var renderer = ZoneRenderer.get(instance.zone.shape().type());

						if (renderer != EmptyZoneRenderer.INSTANCE) {
							boolean hovered = session.zoneClip != null && session.zoneClip.instance() == instance;
							var baseColor = instance.zone.color().withAlpha(50);
							var color = hovered ? baseColor.lerp((float) EasingGroup.SMOOTHSTEP.easeMirrored((RenderSystem.getShaderGameTime() * 800D) % 1D), Color.WHITE.withAlpha(100)) : baseColor;
							var outlineColor = instance.entities.isEmpty() ? Color.WHITE : Color.GREEN;
							renderer.render(Cast.to(instance.zone.shape()), mc, event, delta, color, outlineColor);
						}
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
	public static void adjustFOV(ViewportEvent.ComputeFov event) {
		var mc = Minecraft.getInstance();

		if (mc.screen != null) {
			event.setFOV(event.getFOV() * mc.screen.getZoom(event.getPartialTick()));
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
}
