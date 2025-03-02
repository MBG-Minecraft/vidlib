package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.content.clock.ClockBlockEntityRenderer;
import dev.beast.mods.shimmer.content.clock.ClockContent;
import dev.beast.mods.shimmer.content.clock.ClockPayload;
import dev.beast.mods.shimmer.feature.camerashake.ShakeCameraPayload;
import dev.beast.mods.shimmer.feature.camerashake.StopCameraShakingPayload;
import dev.beast.mods.shimmer.feature.cutscene.PlayCutscenePayload;
import dev.beast.mods.shimmer.feature.cutscene.StopCutscenePayload;
import dev.beast.mods.shimmer.feature.misc.FakeBlockPayload;
import dev.beast.mods.shimmer.feature.misc.SetPostEffectPayload;
import dev.beast.mods.shimmer.feature.multiverse.VoidSpecialEffects;
import dev.beast.mods.shimmer.feature.structure.ClientStructureStorage;
import dev.beast.mods.shimmer.feature.zone.SphereZoneShape;
import dev.beast.mods.shimmer.feature.zone.UniverseZoneShape;
import dev.beast.mods.shimmer.feature.zone.UpdateZonesPayload;
import dev.beast.mods.shimmer.feature.zone.ZoneShapeGroup;
import dev.beast.mods.shimmer.feature.zone.renderer.EmptyZoneRenderer;
import dev.beast.mods.shimmer.feature.zone.renderer.GroupZoneRenderer;
import dev.beast.mods.shimmer.feature.zone.renderer.SphereZoneRenderer;
import dev.beast.mods.shimmer.feature.zone.renderer.ZoneRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
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
		var reg = event.registrar("1").optional();

		reg.playToClient(ClockPayload.TYPE, ClockPayload.STREAM_CODEC, ClockPayload::handle);
		reg.playToClient(UpdateZonesPayload.TYPE, UpdateZonesPayload.STREAM_CODEC, UpdateZonesPayload::handle);
		reg.playToClient(FakeBlockPayload.TYPE, FakeBlockPayload.STREAM_CODEC, FakeBlockPayload::handle);
		reg.playToClient(PlayCutscenePayload.TYPE, PlayCutscenePayload.STREAM_CODEC, PlayCutscenePayload::handle);
		reg.playToClient(StopCutscenePayload.TYPE, StopCutscenePayload.STREAM_CODEC, StopCutscenePayload::handle);
		reg.playToClient(ShakeCameraPayload.TYPE, ShakeCameraPayload.STREAM_CODEC, ShakeCameraPayload::handle);
		reg.playToClient(StopCameraShakingPayload.TYPE, StopCameraShakingPayload.STREAM_CODEC, StopCameraShakingPayload::handle);
		reg.playToClient(SetPostEffectPayload.TYPE, SetPostEffectPayload.STREAM_CODEC, SetPostEffectPayload::handle);
	}

	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(ClockContent.BLOCK_ENTITY.get(), ClockBlockEntityRenderer::new);
	}

	@SubscribeEvent
	public static void registerDimensionSpecialEffects(RegisterDimensionSpecialEffectsEvent event) {
		event.register(Shimmer.id("void"), new VoidSpecialEffects());
	}
}
