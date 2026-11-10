package dev.latvian.mods.vidlib.feature.particle;

import dev.latvian.mods.klib.particle.CustomParticleGroup;
import dev.latvian.mods.vidlib.feature.actor.ActorParticle;
import net.minecraft.client.particle.ParticleRenderType;
import net.neoforged.neoforge.client.event.RegisterParticleGroupsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

public interface VidLibClientParticles {
	ParticleRenderType CUSTOM_RENDER_TYPE = new ParticleRenderType("VIDLIB_CUSTOM", "VL_C");

	static void registerParticleProviders(RegisterParticleProvidersEvent event) {
		event.registerSpecial(VidLibParticles.ACTOR.get(), ActorParticle::create);
		// event.registerSpecial(VidLibParticles.LIGHTNING.get(), LightningParticle::new);
		// event.registerSpriteSet(VidLibParticles.BURN_SMOKE.get(), BurnSmokeParticle::create);
		// event.registerSpriteSet(VidLibParticles.SPARK.get(), SparkParticle::create);
		// event.registerSpriteSet(VidLibParticles.WIND.get(), WindParticle::create);
		// event.registerSpriteSet(VidLibParticles.FIRE.get(), FireParticle::create);
	}

	static void registerParticleGroups(RegisterParticleGroupsEvent event) {
		event.register(CUSTOM_RENDER_TYPE, CustomParticleGroup::new);
	}
}
