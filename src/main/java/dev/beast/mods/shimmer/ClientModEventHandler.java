package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.content.clock.ClockBlockEntityRenderer;
import dev.beast.mods.shimmer.content.clock.ClockContent;
import dev.beast.mods.shimmer.feature.zone.EmptyZone;
import dev.beast.mods.shimmer.feature.zone.UniverseZone;
import dev.beast.mods.shimmer.feature.zone.renderer.EmptyZoneRenderer;
import dev.beast.mods.shimmer.feature.zone.renderer.ZoneRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEventHandler {
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		ZoneRenderer.register(EmptyZone.TYPE, instance -> EmptyZoneRenderer.INSTANCE);
		ZoneRenderer.register(UniverseZone.TYPE, instance -> EmptyZoneRenderer.INSTANCE);
	}

	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(ClockContent.BLOCK_ENTITY.get(), ClockBlockEntityRenderer::new);
	}
}
