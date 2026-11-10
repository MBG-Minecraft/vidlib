package dev.latvian.mods.vidlib.feature.particle;

import dev.latvian.mods.klib.particle.KLibParticles;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.actor.ActorParticleOptions;
import net.minecraft.core.particles.ParticleType;

import java.util.function.Supplier;

public interface VidLibParticles {
	static void init() {
	}

	Supplier<ParticleType<ActorParticleOptions>> ACTOR = KLibParticles.register(ID.vidlib("actor"), () -> ActorParticleOptions.MAP_CODEC, () -> ActorParticleOptions.STREAM_CODEC);
	// Supplier<ParticleType<LightningParticleOptions>> LIGHTNING = KLibParticles.register(ID.vidlib("lightning"), () -> LightningParticleOptions.MAP_CODEC, () -> LightningParticleOptions.STREAM_CODEC);
	// Supplier<SimpleParticleType> BURN_SMOKE = KLibParticles.register(ID.vidlib("burn_smoke"));
	// Supplier<SimpleParticleType> SPARK = KLibParticles.register(ID.vidlib("spark"));
	// Supplier<ParticleType<WindParticleOptions>> WIND = KLibParticles.register(ID.vidlib("wind"), () -> WindParticleOptions.MAP_CODEC, () -> WindParticleOptions.STREAM_CODEC);
	// Supplier<ParticleType<FireParticleOptions>> FIRE = KLibParticles.register(ID.vidlib("fire"), () -> FireParticleOptions.MAP_CODEC, () -> FireParticleOptions.STREAM_CODEC);
}
