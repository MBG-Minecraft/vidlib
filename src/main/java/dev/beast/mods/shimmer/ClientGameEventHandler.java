package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.content.clock.ClockBlockEntity;
import dev.beast.mods.shimmer.feature.zone.ActiveZones;
import dev.beast.mods.shimmer.feature.zone.renderer.ZoneRenderer;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientGameEventHandler {
	@SubscribeEvent
	public static void clientPreTick(ClientTickEvent.Pre event) {
		var mc = Minecraft.getInstance();

		if (mc.level != null) {
			ClockBlockEntity.tick();

			for (var container : ActiveZones.CLIENT) {
				if (container.dimension == mc.level.dimension()) {
					container.tick(mc.level);
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

		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
			for (var container : ActiveZones.CLIENT) {
				if (container.dimension == mc.level.dimension()) {
					ZoneRenderer.renderAll(container, mc, event);
				}
			}
		}
	}
}
