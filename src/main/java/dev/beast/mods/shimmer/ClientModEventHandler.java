package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.feature.multiverse.VoidSpecialEffects;
import dev.beast.mods.shimmer.feature.particle.ShimmerClientParticles;
import dev.beast.mods.shimmer.feature.structure.ClientStructureStorage;
import dev.beast.mods.shimmer.feature.structure.GhostStructure;
import dev.beast.mods.shimmer.feature.zone.RotatedBoxZoneShape;
import dev.beast.mods.shimmer.feature.zone.SphereZoneShape;
import dev.beast.mods.shimmer.feature.zone.UniverseZoneShape;
import dev.beast.mods.shimmer.feature.zone.ZoneShapeGroup;
import dev.beast.mods.shimmer.feature.zone.renderer.EmptyZoneRenderer;
import dev.beast.mods.shimmer.feature.zone.renderer.GroupZoneRenderer;
import dev.beast.mods.shimmer.feature.zone.renderer.RotatedBoxZoneRenderer;
import dev.beast.mods.shimmer.feature.zone.renderer.SphereZoneRenderer;
import dev.beast.mods.shimmer.feature.zone.renderer.ZoneRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEventHandler {
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		ZoneRenderer.register(UniverseZoneShape.TYPE, EmptyZoneRenderer.INSTANCE);
		ZoneRenderer.register(ZoneShapeGroup.TYPE, new GroupZoneRenderer());
		ZoneRenderer.register(SphereZoneShape.TYPE, new SphereZoneRenderer());
		ZoneRenderer.register(RotatedBoxZoneShape.TYPE, new RotatedBoxZoneRenderer());
	}

	@SubscribeEvent
	public static void addReloadListeners(RegisterClientReloadListenersEvent event) {
		event.registerReloadListener(ClientStructureStorage.CLIENT);
		event.registerReloadListener(new GhostStructure.Loader());
	}

	@SubscribeEvent
	public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
		ShimmerClientParticles.register(event);
	}

	@SubscribeEvent
	public static void registerDimensionSpecialEffects(RegisterDimensionSpecialEffectsEvent event) {
		event.register(Shimmer.id("void"), new VoidSpecialEffects());
	}
}
