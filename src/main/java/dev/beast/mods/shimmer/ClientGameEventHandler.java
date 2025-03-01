package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.content.clock.ClockBlockEntity;
import dev.beast.mods.shimmer.feature.cutscene.ClientCutscene;
import dev.beast.mods.shimmer.feature.zone.ActiveZones;
import dev.beast.mods.shimmer.feature.zone.renderer.EmptyZoneRenderer;
import dev.beast.mods.shimmer.feature.zone.renderer.ZoneRenderer;
import dev.beast.mods.shimmer.math.Color;
import dev.beast.mods.shimmer.math.KMath;
import dev.beast.mods.shimmer.util.Cast;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientGameEventHandler {
	@SubscribeEvent
	public static void clientPreTick(ClientTickEvent.Pre event) {
		var mc = Minecraft.getInstance();

		if (mc.level != null) {
			ClockBlockEntity.tick();

			ActiveZones.CLIENT.entityZones.clear();

			for (var container : ActiveZones.CLIENT) {
				if (container.dimension == mc.level.dimension()) {
					container.tick(ActiveZones.CLIENT, mc.level);
				}
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

		if (mc.level == null) {
			return;
		}

		float delta = event.getPartialTick().getGameTimeDeltaPartialTick(true);

		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
			if (mc.getEntityRenderDispatcher().shouldRenderHitBoxes()) {
				for (var container : ActiveZones.CLIENT) {
					if (container.dimension == mc.level.dimension()) {
						for (var instance : container.zones) {
							var renderer = ZoneRenderer.get(instance.zone.shape().type());

							if (renderer != EmptyZoneRenderer.INSTANCE) {
								renderer.render(Cast.to(instance.zone.shape()), mc, event, delta, instance.zone.color().withAlpha(0.2F), instance.entities.isEmpty() ? Color.WHITE : Color.GREEN);
							}
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
}
