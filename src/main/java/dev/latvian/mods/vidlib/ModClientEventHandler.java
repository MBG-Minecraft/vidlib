package dev.latvian.mods.vidlib;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.BlockEntityRendererHolder;
import dev.latvian.mods.vidlib.feature.auto.EntityRendererHolder;
import dev.latvian.mods.vidlib.feature.canvas.CanvasImpl;
import dev.latvian.mods.vidlib.feature.client.VidLibKeys;
import dev.latvian.mods.vidlib.feature.clock.Clock;
import dev.latvian.mods.vidlib.feature.clock.ClockFont;
import dev.latvian.mods.vidlib.feature.clothing.ClientClothingLoader;
import dev.latvian.mods.vidlib.feature.entity.progress.ProgressBarRenderer;
import dev.latvian.mods.vidlib.feature.gradient.ClientGradients;
import dev.latvian.mods.vidlib.feature.location.Location;
import dev.latvian.mods.vidlib.feature.multiverse.VoidSpecialEffects;
import dev.latvian.mods.vidlib.feature.particle.VidLibClientParticles;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleData;
import dev.latvian.mods.vidlib.feature.skybox.SkyboxData;
import dev.latvian.mods.vidlib.feature.structure.GhostStructure;
import dev.latvian.mods.vidlib.feature.structure.StructureStorage;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
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
	public static void addReloadListeners(AddClientReloadListenersEvent event) {
		event.addListener(VidLib.id("location"), new Location.Loader(Location.CLIENT_REGISTRY));
		event.addListener(VidLib.id("structure"), new StructureStorage(StructureStorage.CLIENT));
		event.addListener(VidLib.id("ghost_structure"), new GhostStructure.Loader());
		event.addListener(VidLib.id("clothing"), new ClientClothingLoader());
		event.addListener(VidLib.id("physics_particle_data"), new PhysicsParticleData.Loader());
		event.addListener(VidLib.id("gradient"), new ClientGradients());
		event.addListener(VidLib.id("clock_font"), new ClockFont.Loader());
		event.addListener(VidLib.id("clock"), new Clock.Loader());
		event.addListener(VidLib.id("skybox"), new SkyboxData.Loader());

		event.addDependency(VidLib.id("location"), VidLib.id("ghost_structure"));
		event.addDependency(VidLib.id("structure"), VidLib.id("ghost_structure"));
		event.addDependency(VidLib.id("location"), VidLib.id("clock"));
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
		event.registerAbove(VanillaGuiLayers.BOSS_OVERLAY, VidLib.id("above_boss"), ModClientEventHandler::drawAboveBossOverlay);
	}

	public static void drawAboveBossOverlay(GuiGraphics graphics, DeltaTracker deltaTracker) {
		var mc = Minecraft.getInstance();
		ProgressBarRenderer.draw(mc, graphics, deltaTracker);
		CanvasImpl.drawPreview(mc, graphics);
	}
}
