package dev.beast.mods.shimmer.feature.shader;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.particle.physics.PhysicsParticleManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ShimmerShaders {
	@SubscribeEvent
	public static void registerShaders(RegisterShadersEvent event) throws IOException {
		PhysicsParticleManager.SOLID.shader.register(event);
		PhysicsParticleManager.CUTOUT.shader.register(event);
		PhysicsParticleManager.TRANSLUCENT.shader.register(event);
	}
}
