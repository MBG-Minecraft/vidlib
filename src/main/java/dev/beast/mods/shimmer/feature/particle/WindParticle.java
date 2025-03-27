package dev.beast.mods.shimmer.feature.particle;

import dev.beast.mods.shimmer.math.KMath;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;

public class WindParticle extends TargetedParticle {
	public static final SingleQuadParticle.FacingCameraMode GROUND = (quaternion, camera, delta) -> {
		quaternion.rotateX(-(float) (Math.PI / 2D));
	};

	public static ParticleProvider<WindParticleOptions> create(SpriteSet spriteSet) {
		return (options, level, x, y, z, xd, yd, zd) -> new WindParticle(options, level, x, y, z, xd, yd, zd, spriteSet);
	}

	public final WindParticleOptions options;
	public final SpriteSet spriteSet;

	public WindParticle(WindParticleOptions options, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet spriteSet) {
		super(level, x, y, z, xd, yd, zd, options.easing());
		this.hasPhysics = false;
		this.options = options;
		this.spriteSet = spriteSet;
		this.lifetime = (int) (options.lifespan() * (1F + random.nextFloat() * 0.2F - 0.1F));
		this.setSpriteFromAge(spriteSet);
		this.quadSize *= 10F;
		this.rCol = this.gCol = this.bCol = 0.8F + random.nextFloat() * 0.2F;
	}

	@Override
	public FacingCameraMode getFacingCameraMode() {
		return options.ground() ? GROUND : SingleQuadParticle.FacingCameraMode.LOOKAT_XYZ;
	}

	@Override
	protected int getLightColor(float tint) {
		return 15728880;
	}

	@Override
	public void tick() {
		super.tick();
		setSpriteFromAge(spriteSet);
		quadSizeMod = KMath.lerp(easing.ease(age / (float) lifetime), 0.25F, 1F) * options.scale();
	}
}
