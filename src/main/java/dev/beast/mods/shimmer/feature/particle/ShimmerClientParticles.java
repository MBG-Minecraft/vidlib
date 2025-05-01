package dev.beast.mods.shimmer.feature.particle;

import dev.beast.mods.shimmer.feature.npc.NPCParticle;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

public class ShimmerClientParticles {
	public static void register(RegisterParticleProvidersEvent event) {
		event.registerSpecial(ShimmerParticles.CUBE.get(), CubeParticle::new);
		event.registerSpecial(ShimmerParticles.LINE.get(), LineParticle::new);
		event.registerSpecial(ShimmerParticles.TEXT.get(), TextParticle::new);
		event.registerSpecial(ShimmerParticles.ITEM.get(), ItemParticle::new);
		event.registerSpecial(ShimmerParticles.NPC.get(), NPCParticle::new);
		event.registerSpriteSet(ShimmerParticles.BURN_SMOKE.get(), BurnSmokeParticle::create);
		event.registerSpriteSet(ShimmerParticles.SPARK.get(), SparkParticle::create);
		event.registerSpriteSet(ShimmerParticles.WIND.get(), WindParticle::create);
		event.registerSpriteSet(ShimmerParticles.FIRE.get(), FireParticle::create);
	}
}
