package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.BlockEntityRendererHolder;
import dev.beast.mods.shimmer.feature.auto.EntityRendererHolder;
import dev.beast.mods.shimmer.feature.multiverse.VoidSpecialEffects;
import dev.beast.mods.shimmer.feature.particle.ShimmerClientParticles;
import dev.beast.mods.shimmer.feature.structure.ClientStructureStorage;
import dev.beast.mods.shimmer.feature.structure.GhostStructure;
import dev.beast.mods.shimmer.util.Cast;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEventHandler {
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		AutoInit.Type.CLIENT_SETUP.invoke();
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

	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		for (var s : AutoRegister.SCANNED.get()) {
			if (s.value() instanceof EntityRendererHolder<?>(java.util.function.Supplier<net.minecraft.world.entity.EntityType<?>> type, net.minecraft.client.renderer.entity.EntityRendererProvider<?> renderer)) {
				event.registerEntityRenderer(Cast.to(type.get()), renderer);
			} else if (s.value() instanceof BlockEntityRendererHolder<?>(java.util.function.Supplier<net.minecraft.world.level.block.entity.BlockEntityType<?>> type, net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider<?> renderer)) {
				event.registerBlockEntityRenderer(Cast.to(type.get()), renderer);
			}
		}
	}

	@SubscribeEvent
	public static void addLayers(EntityRenderersEvent.AddLayers event) {
	}
}
