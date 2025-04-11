package dev.beast.mods.shimmer.feature.particle;

import dev.latvian.mods.kmath.KMath;
import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.kmath.color.Gradient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;

public class FireParticle extends TargetedParticle {
	public static ParticleProvider<FireParticleOptions> create(SpriteSet spriteSet) {
		return (options, level, x, y, z, xd, yd, zd) -> new FireParticle(options, level, x, y, z, xd, yd, zd, spriteSet);
	}

	public final FireParticleOptions options;
	public final SpriteSet spriteSet;
	public final Gradient gradient;
	public final float randomOffset;

	public FireParticle(FireParticleOptions options, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet spriteSet) {
		super(level, x, y, z, xd, yd, zd, options.easing());
		this.hasPhysics = false;
		this.options = options;
		this.spriteSet = spriteSet;
		this.gradient = options.resolveGradient();
		this.lifetime = (int) (options.lifespan() * (1F + random.nextFloat() * 0.2F - 0.1F));
		this.pickSprite(spriteSet);
		this.quadSize *= 2F;
		this.randomOffset = 0.8F + random.nextFloat() * 0.4F;
		var color = (gradient == null ? Color.WHITE : gradient).get(0F);
		setColor(color.redf(), color.greenf(), color.bluef());
	}

	@Override
	protected int getLightColor(float tint) {
		return 15728880;
	}

	@Override
	public void tick() {
		super.tick();

		if (age > lifetime - 3) {
			alpha = 1F - (age - lifetime + 3) / 3F;
			quadSizeMod = KMath.lerp(easing.ease(age / (float) lifetime), 0.25F, 1F) * options.scale() * alpha;
		} else {
			alpha = 1F;
			quadSizeMod = KMath.lerp(easing.ease(age / (float) lifetime), 0.25F, 1F) * options.scale();
		}

		var color = (gradient == null ? Color.WHITE : gradient).get(age * randomOffset / (float) lifetime);
		setColor(color.redf(), color.greenf(), color.bluef());
	}
}
