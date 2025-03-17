package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.BlockEntityRendererHolder;
import dev.beast.mods.shimmer.feature.auto.EntityRendererHolder;
import dev.beast.mods.shimmer.feature.clothing.Clothing;
import dev.beast.mods.shimmer.feature.misc.MiscShimmerClientUtils;
import dev.beast.mods.shimmer.feature.multiverse.VoidSpecialEffects;
import dev.beast.mods.shimmer.feature.particle.ShimmerClientParticles;
import dev.beast.mods.shimmer.feature.structure.ClientStructureStorage;
import dev.beast.mods.shimmer.feature.structure.GhostStructure;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEventHandler {
	@SubscribeEvent
	public static void addReloadListeners(AddClientReloadListenersEvent event) {
		event.addListener(Shimmer.id("structure"), ClientStructureStorage.CLIENT);
		event.addListener(Shimmer.id("ghost_structure"), new GhostStructure.Loader());
	}

	@SubscribeEvent
	public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
		ShimmerClientParticles.register(event);
	}

	@SubscribeEvent
	public static void registerDimensionSpecialEffects(RegisterDimensionSpecialEffectsEvent event) {
		event.register(Shimmer.id("void"), new VoidSpecialEffects());
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
		event.registerEntityModifier(PlayerRenderer.class, ClientModEventHandler::modifyPlayerRenderState);
	}

	private static void modifyPlayerRenderState(AbstractClientPlayer player, PlayerRenderState state) {
		if (player.isCreative()) {
			state.setRenderData(MiscShimmerClientUtils.CREATIVE, Boolean.TRUE);
		}

		if (!player.isInvisible()) {
			var clothing = player.shimmer$sessionData().clothing;

			if (clothing != Clothing.NONE) {
				state.setRenderData(MiscShimmerClientUtils.CLOTHING, clothing);
			}
		}
	}
}
