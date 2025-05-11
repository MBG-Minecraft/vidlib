package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.client.ParticleRandom;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Particle.class)
public abstract class ParticleMixin {
	@Redirect(method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;create()Lnet/minecraft/util/RandomSource;"))
	private RandomSource vl$createRandomSource() {
		return ParticleRandom.CURRENT;
	}
}
