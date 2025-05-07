package dev.latvian.mods.vidlib;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.BlockEntityRendererHolder;
import dev.latvian.mods.vidlib.feature.auto.EntityRendererHolder;
import dev.latvian.mods.vidlib.feature.client.VidLibKeys;
import dev.latvian.mods.vidlib.feature.clock.Clock;
import dev.latvian.mods.vidlib.feature.clock.ClockFont;
import dev.latvian.mods.vidlib.feature.clothing.ClientClothingLoader;
import dev.latvian.mods.vidlib.feature.clothing.Clothing;
import dev.latvian.mods.vidlib.feature.entity.progress.ProgressBarRenderer;
import dev.latvian.mods.vidlib.feature.gradient.ClientGradients;
import dev.latvian.mods.vidlib.feature.location.Location;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import dev.latvian.mods.vidlib.feature.multiverse.VoidSpecialEffects;
import dev.latvian.mods.vidlib.feature.particle.VidLibClientParticles;
import dev.latvian.mods.vidlib.feature.particle.VidLibParticleRenderTypes;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleData;
import dev.latvian.mods.vidlib.feature.skybox.SkyboxData;
import dev.latvian.mods.vidlib.feature.structure.GhostStructure;
import dev.latvian.mods.vidlib.feature.structure.StructureStorage;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
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
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;

import java.util.ArrayList;

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

		VidLibParticleRenderTypes.TEMP_LIST.put(VidLibParticleRenderTypes.TRUE_TRANSLUCENT, new ArrayList<>());
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
	public static void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
		event.registerEntityModifier(PlayerRenderer.class, ModClientEventHandler::modifyPlayerRenderState);
	}

	private static void modifyPlayerRenderState(AbstractClientPlayer player, PlayerRenderState state) {
		if (player.isCreative()) {
			state.setRenderData(MiscClientUtils.CREATIVE, Boolean.TRUE);
		}

		var vehicle = player.getVehicle();

		if (vehicle != null) {
			var s = vehicle.getPassengerScale(player);

			if (s <= 0F) {
				state.isInvisible = true;
			} else {
				state.scale *= s;
			}
		}

		var session = player.vl$sessionData();

		var clothing = state.isInvisible ? null : player.getClothing();
		state.setRenderData(MiscClientUtils.CLOTHING, clothing == Clothing.NONE ? null : clothing);

		if (state.nameTag != null) {
			state.nameTag = session.modifyPlayerName(state.nameTag);
		}

		if (session.scoreText != null) {
			state.scoreText = session.scoreText;
		}
	}

	@SubscribeEvent
	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		VidLibKeys.register(event);
	}

	@SubscribeEvent
	public static void registerGuiLayers(RegisterGuiLayersEvent event) {
		event.registerAbove(VanillaGuiLayers.BOSS_OVERLAY, ProgressBarRenderer.ID, ProgressBarRenderer::render);
	}
}
