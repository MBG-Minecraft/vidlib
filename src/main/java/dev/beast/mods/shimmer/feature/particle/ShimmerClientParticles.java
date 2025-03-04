package dev.beast.mods.shimmer.feature.particle;

import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

public class ShimmerClientParticles {
	public static void register(RegisterParticleProvidersEvent event) {
		event.registerSpecial(ShimmerParticles.CUBE.get(), CubeParticle::new);
	}
}
