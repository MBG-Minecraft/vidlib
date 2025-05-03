package dev.latvian.mods.vidlib.feature.particle;

import dev.latvian.mods.vidlib.feature.npc.NPCParticle;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

public interface VidLibClientParticles {
	static void register(RegisterParticleProvidersEvent event) {
		event.registerSpecial(VidLibParticles.CUBE.get(), CubeParticle::new);
		event.registerSpecial(VidLibParticles.LINE.get(), LineParticle::new);
		event.registerSpecial(VidLibParticles.TEXT.get(), TextParticle::new);
		event.registerSpecial(VidLibParticles.ITEM.get(), ItemParticle::new);
		event.registerSpecial(VidLibParticles.NPC.get(), NPCParticle::new);
		event.registerSpriteSet(VidLibParticles.BURN_SMOKE.get(), BurnSmokeParticle::create);
		event.registerSpriteSet(VidLibParticles.SPARK.get(), SparkParticle::create);
		event.registerSpriteSet(VidLibParticles.WIND.get(), WindParticle::create);
		event.registerSpriteSet(VidLibParticles.FIRE.get(), FireParticle::create);
	}
}
