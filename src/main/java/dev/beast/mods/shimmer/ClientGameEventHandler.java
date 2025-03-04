package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.feature.clock.ClockRenderer;
import dev.beast.mods.shimmer.feature.cutscene.ClientCutscene;
import dev.beast.mods.shimmer.feature.zone.renderer.EmptyZoneRenderer;
import dev.beast.mods.shimmer.feature.zone.renderer.ZoneRenderer;
import dev.beast.mods.shimmer.math.Color;
import dev.beast.mods.shimmer.math.KMath;
import dev.beast.mods.shimmer.util.Cast;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientGameEventHandler {
	@SubscribeEvent
	public static void clientPreTick(ClientTickEvent.Pre event) {
		Minecraft.getInstance().shimmer$preTick();
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

		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
			Minecraft.getInstance().shimmer$renderSetup(event, delta);
		} else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
			if (mc.getEntityRenderDispatcher().shouldRenderHitBoxes()) {
				for (var container : session.filteredZones) {
					for (var instance : container.zones) {
						var renderer = ZoneRenderer.get(instance.zone.shape().type());

						if (renderer != EmptyZoneRenderer.INSTANCE) {
							boolean hovered = session.zoneClip != null && session.zoneClip.instance() == instance;
							var baseColor = instance.zone.color().withAlpha(50);
							var outlineColor = hovered ? Color.WHITE : instance.entities.isEmpty() ? baseColor : Color.GREEN;
							renderer.render(Cast.to(instance.zone.shape()), mc, event, delta, baseColor, outlineColor);
						}
					}
				}
			}

			for (var instance : session.clocks.values()) {
				if (instance.clock.dimension() == mc.level.dimension()) {
					for (var location : instance.clock.locations()) {
						ClockRenderer.render(mc, instance, location, event, delta);
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

	@SubscribeEvent
	public static void debugText(CustomizeGuiOverlayEvent.DebugText event) {
		var mc = Minecraft.getInstance();

		// event.getLeft().add(mc.fpsString);

		// event.getLeft().clear();
		// event.getRight().clear();

		if (mc.player != null && mc.player.getMainHandItem().has(DataComponents.CUSTOM_DATA)) {
			var toolType = mc.player.getMainHandItem().get(DataComponents.CUSTOM_DATA).getUnsafe().getString("shimmer:tool");

			if (toolType.equals("pos")) {
				if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK && mc.hitResult instanceof BlockHitResult hit) {
					event.getRight().add("%d, %d, %d".formatted(hit.getBlockPos().getX(), hit.getBlockPos().getY(), hit.getBlockPos().getZ()));
				}
			}
		}
	}
}
