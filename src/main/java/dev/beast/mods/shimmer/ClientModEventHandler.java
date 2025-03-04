package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.core.ShimmerPayloadRegistrar;
import dev.beast.mods.shimmer.feature.camerashake.ShakeCameraPayload;
import dev.beast.mods.shimmer.feature.camerashake.StopCameraShakingPayload;
import dev.beast.mods.shimmer.feature.clock.SyncClockFontsPayload;
import dev.beast.mods.shimmer.feature.clock.SyncClockInstancePayload;
import dev.beast.mods.shimmer.feature.clock.SyncClocksPayload;
import dev.beast.mods.shimmer.feature.cutscene.PlayCutscenePayload;
import dev.beast.mods.shimmer.feature.cutscene.StopCutscenePayload;
import dev.beast.mods.shimmer.feature.misc.CreateFireworksPayload;
import dev.beast.mods.shimmer.feature.misc.FakeBlockPayload;
import dev.beast.mods.shimmer.feature.misc.SetPostEffectPayload;
import dev.beast.mods.shimmer.feature.multiverse.VoidSpecialEffects;
import dev.beast.mods.shimmer.feature.structure.ClientStructureStorage;
import dev.beast.mods.shimmer.feature.zone.SphereZoneShape;
import dev.beast.mods.shimmer.feature.zone.SyncZonesPayload;
import dev.beast.mods.shimmer.feature.zone.UniverseZoneShape;
import dev.beast.mods.shimmer.feature.zone.ZoneShapeGroup;
import dev.beast.mods.shimmer.feature.zone.renderer.EmptyZoneRenderer;
import dev.beast.mods.shimmer.feature.zone.renderer.GroupZoneRenderer;
import dev.beast.mods.shimmer.feature.zone.renderer.SphereZoneRenderer;
import dev.beast.mods.shimmer.feature.zone.renderer.ZoneRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEventHandler {
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		ZoneRenderer.register(UniverseZoneShape.TYPE, EmptyZoneRenderer.INSTANCE);
		ZoneRenderer.register(ZoneShapeGroup.TYPE, new GroupZoneRenderer());
		ZoneRenderer.register(SphereZoneShape.TYPE, new SphereZoneRenderer());
	}

	@SubscribeEvent
	public static void addReloadListeners(RegisterClientReloadListenersEvent event) {
		event.registerReloadListener(ClientStructureStorage.CLIENT);
	}

	@SubscribeEvent
	static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
		var reg = ShimmerPayloadRegistrar.of(event);

		reg.s2c(SyncZonesPayload.TYPE);
		reg.s2c(SyncClockFontsPayload.TYPE);
		reg.s2c(SyncClocksPayload.TYPE);

		reg.s2c(FakeBlockPayload.TYPE);
		reg.s2c(PlayCutscenePayload.TYPE);
		reg.s2c(StopCutscenePayload.TYPE);
		reg.s2c(ShakeCameraPayload.TYPE);
		reg.s2c(StopCameraShakingPayload.TYPE);
		reg.s2c(SetPostEffectPayload.TYPE);
		reg.s2c(SyncClockInstancePayload.TYPE);
		reg.s2c(CreateFireworksPayload.TYPE);
	}

	@SubscribeEvent
	public static void registerDimensionSpecialEffects(RegisterDimensionSpecialEffectsEvent event) {
		event.register(Shimmer.id("void"), new VoidSpecialEffects());
	}
}
