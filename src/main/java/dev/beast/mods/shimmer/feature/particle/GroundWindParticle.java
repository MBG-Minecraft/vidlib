package dev.beast.mods.shimmer.feature.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class GroundWindParticle extends WindParticle {
	public static final SingleQuadParticle.FacingCameraMode GROUND = (quaternion, camera, delta) -> {
		quaternion.rotateX(-(float) (Math.PI / 2D));
	};

	public static ParticleProvider<SimpleParticleType> create(SpriteSet spriteSet) {
		return (type, level, x, y, z, xd, yd, zd) -> new GroundWindParticle(level, x, y, z, xd, yd, zd, spriteSet);
	}

	public GroundWindParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet spriteSet) {
		super(level, x, y, z, xd, yd, zd, spriteSet);
	}

	@Override
	public FacingCameraMode getFacingCameraMode() {
		return GROUND;
	}
}
