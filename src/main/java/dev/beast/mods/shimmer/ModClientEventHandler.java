package dev.beast.mods.shimmer;

import com.mojang.blaze3d.platform.InputConstants;
import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.BlockEntityRendererHolder;
import dev.beast.mods.shimmer.feature.auto.EntityRendererHolder;
import dev.beast.mods.shimmer.feature.clothing.ClientClothingLoader;
import dev.beast.mods.shimmer.feature.clothing.Clothing;
import dev.beast.mods.shimmer.feature.gradient.ClientGradients;
import dev.beast.mods.shimmer.feature.misc.MiscShimmerClientUtils;
import dev.beast.mods.shimmer.feature.multiverse.VoidSpecialEffects;
import dev.beast.mods.shimmer.feature.particle.ShimmerClientParticles;
import dev.beast.mods.shimmer.feature.particle.ShimmerParticleRenderTypes;
import dev.beast.mods.shimmer.feature.particle.physics.PhysicsParticleData;
import dev.beast.mods.shimmer.feature.structure.GhostStructure;
import dev.beast.mods.shimmer.feature.structure.StructureStorage;
import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientEventHandler {
	@SubscribeEvent
	public static void addReloadListeners(AddClientReloadListenersEvent event) {
		event.addListener(Shimmer.id("structure"), new StructureStorage(StructureStorage.CLIENT));
		event.addListener(Shimmer.id("ghost_structure"), new GhostStructure.Loader());
		event.addListener(Shimmer.id("clothing"), new ClientClothingLoader());
		event.addListener(Shimmer.id("physics_particle_data"), new PhysicsParticleData.Loader());
		event.addListener(Shimmer.id("gradient"), new ClientGradients());
	}

	@SubscribeEvent
	public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
		ShimmerClientParticles.register(event);

		ShimmerParticleRenderTypes.TEMP_LIST.put(ShimmerParticleRenderTypes.TRUE_TRANSLUCENT, new ArrayList<>());
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
			state.setRenderData(MiscShimmerClientUtils.CREATIVE, Boolean.TRUE);
		}

		var vehicle = player.getVehicle();

		if (vehicle != null && vehicle.hidePassenger(player)) {
			state.isInvisible = true;
		}

		var session = player.shimmer$sessionData();

		var clothing = state.isInvisible ? null : player.getClothing();
		state.setRenderData(MiscShimmerClientUtils.CLOTHING, clothing == Clothing.NONE ? null : clothing);

		if (state.nameTag != null) {
			state.nameTag = session.modifyPlayerName(state.nameTag);
		}

		if (session.scoreText != null) {
			state.scoreText = session.scoreText;
		}
	}

	@SubscribeEvent
	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(MiscShimmerClientUtils.freezeTickKeyMapping = new KeyMapping("key.vidlib.freeze_tick", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_P, "key.categories.vidlib"));
		event.register(MiscShimmerClientUtils.clearParticlesKeyMapping = new KeyMapping("key.vidlib.clear_particles", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_L, "key.categories.vidlib"));
	}
}
