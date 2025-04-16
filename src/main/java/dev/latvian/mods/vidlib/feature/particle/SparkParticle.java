package dev.latvian.mods.vidlib.feature.particle;

import dev.latvian.mods.kmath.KMath;
import dev.latvian.mods.vidlib.feature.gradient.ClientGradients;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import org.joml.Math;

public class SparkParticle extends TextureSheetParticle {
	public static ParticleProvider<SimpleParticleType> create(SpriteSet spriteSet) {
		return (type, level, x, y, z, xd, yd, zd) -> new SparkParticle(level, x, y, z, xd, yd, zd, spriteSet);
	}

	private final SpriteSet spriteSet;
	private final float randomOffset;

	public SparkParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet spriteSet) {
		super(level, x, y, z);
		this.spriteSet = spriteSet;
		this.pickSprite(spriteSet);
		this.xd = xd;
		this.yd = yd;
		this.zd = zd;
		this.friction = 0.9F;
		this.randomOffset = random.nextFloat();
		this.lifetime = (int) KMath.lerp(randomOffset, 15, org.joml.Math.max(30, Math.sqrt(xd * xd + yd * yd + zd * zd) * 10));
		this.gravity = KMath.lerp(randomOffset, 1.1F, 1.5F);
	}

	@Override
	public ParticleRenderType getRenderType() {
		return VidLibParticleRenderTypes.TRANSLUCENT_ADDITION;
	}

	@Override
	public void tick() {
		hasPhysics = yd < 0D;
		oRoll = roll;
		super.tick();
		setSpriteFromAge(spriteSet);
		var col = ClientGradients.SPARK.get().get(age / (float) lifetime + KMath.lerp(randomOffset, -0.3F, 0.3F));
		setColor(col.redf(), col.greenf(), col.bluef());
		alpha = 1F - (age / (float) lifetime);
		quadSize = alpha * 0.2F;
		roll = KMath.lerp(randomOffset, -1F, 1F) * alpha;
	}

	@Override
	protected int getLightColor(float tint) {
		return 15728880;
	}
}
