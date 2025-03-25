package dev.beast.mods.shimmer.feature.particle;

import dev.beast.mods.shimmer.math.Easing;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class WindParticle extends TargetedParticle {
	public static ParticleProvider<SimpleParticleType> create(SpriteSet spriteSet) {
		return (type, level, x, y, z, xd, yd, zd) -> new WindParticle(level, x, y, z, xd, yd, zd, spriteSet);
	}

	public WindParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet spriteSet) {
		super(level, x, y, z, xd, yd, zd, Easing.EXPO_OUT);
		this.pickSprite(spriteSet);
		this.lifetime = 100;
		this.quadSize *= 10F;
		this.rCol = this.gCol = this.bCol = 0.8F + random.nextFloat() * 0.2F;
	}

	@Override
	protected int getLightColor(float tint) {
		return 15728880;
	}
}
