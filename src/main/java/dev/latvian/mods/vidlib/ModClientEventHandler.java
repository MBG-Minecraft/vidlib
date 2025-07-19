package dev.latvian.mods.vidlib;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.BlockEntityRendererHolder;
import dev.latvian.mods.vidlib.feature.auto.EntityRendererHolder;
import dev.latvian.mods.vidlib.feature.client.VidLibHUD;
import dev.latvian.mods.vidlib.feature.client.VidLibKeys;
import dev.latvian.mods.vidlib.feature.clock.Clock;
import dev.latvian.mods.vidlib.feature.clock.ClockFont;
import dev.latvian.mods.vidlib.feature.clothing.ClientClothingLoader;
import dev.latvian.mods.vidlib.feature.gradient.ClientGradientLoader;
import dev.latvian.mods.vidlib.feature.multiverse.VoidSpecialEffects;
import dev.latvian.mods.vidlib.feature.particle.VidLibClientParticles;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleData;
import dev.latvian.mods.vidlib.feature.skybox.SkyboxData;
import dev.latvian.mods.vidlib.feature.structure.GhostStructure;
import dev.latvian.mods.vidlib.feature.structure.StructureStorage;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = VidLib.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientEventHandler {
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(ModClientEventHandler::syncSetup);
	}

	public static void syncSetup() {
		RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS).getBuffer(1).setLabel("Shared Sequential Quads Buffer");
		RenderSystem.getSequentialBuffer(VertexFormat.Mode.LINES).getBuffer(1).setLabel("Shared Sequential Lines Buffer");
		RenderSystem.getSequentialBuffer(VertexFormat.Mode.TRIANGLES).getBuffer(1).setLabel("Shared Sequential Other Buffer");
	}

	@SubscribeEvent
	public static void addReloadListeners(AddClientReloadListenersEvent event) {
		event.addListener(VidLib.id("structure"), new StructureStorage(StructureStorage.CLIENT));
		event.addListener(VidLib.id("ghost_structure"), new GhostStructure.Loader());
		event.addListener(VidLib.id("clothing"), new ClientClothingLoader());
		event.addListener(VidLib.id("physics_particle_data"), new PhysicsParticleData.Loader());
		event.addListener(VidLib.id("gradient"), new ClientGradientLoader());
		event.addListener(VidLib.id("clock_font"), new ClockFont.Loader());
		event.addListener(VidLib.id("clock"), new Clock.Loader());
		event.addListener(VidLib.id("skybox"), new SkyboxData.Loader());

		event.addDependency(VidLib.id("structure"), VidLib.id("ghost_structure"));
		event.addDependency(VidLib.id("clock_font"), VidLib.id("clock"));
	}

	@SubscribeEvent
	public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
		VidLibClientParticles.register(event);
	}

	@SubscribeEvent
	public static void registerDimensionSpecialEffects(RegisterDimensionSpecialEffectsEvent event) {
		event.register(VidLib.id("void"), new VoidSpecialEffects());
	}

	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		for (var s : AutoRegister.SCANNED.get()) {
			if (s.value() instanceof EntityRendererHolder<?> holder) {
				holder.register(event);
			} else if (s.value() instanceof BlockEntityRendererHolder<?> holder) {
				holder.register(event);
			}
		}
	}

	@SubscribeEvent
	public static void addLayers(EntityRenderersEvent.AddLayers event) {
	}

	@SubscribeEvent
	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		VidLibKeys.register(event);
	}

	@SubscribeEvent
	public static void registerGuiLayers(RegisterGuiLayersEvent event) {
		event.registerBelowAll(VidLib.id("player_names"), VidLibHUD::drawPlayerNames);
		event.registerAbove(VanillaGuiLayers.BOSS_OVERLAY, VidLib.id("above_boss"), VidLibHUD::drawAboveBossOverlay);
		event.registerAboveAll(VidLib.id("fade"), VidLibHUD::drawFade);
	}
}
