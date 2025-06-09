package dev.latvian.mods.vidlib.feature.particle;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.klib.math.KMath;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;

public class FireParticle extends InterpolatedParticle {
	public static ParticleProvider<FireParticleOptions> create(SpriteSet spriteSet) {
		return (options, level, x, y, z, xd, yd, zd) -> new FireParticle(options, level, x, y, z, xd, yd, zd, spriteSet);
	}

	public final FireParticleOptions options;
	public final SpriteSet spriteSet;
	public final Gradient gradient;
	public final float randomOffset;

	public FireParticle(FireParticleOptions options, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet spriteSet) {
		super(level, x, y, z, xd, yd, zd, options.easing());
		this.options = options;
		this.spriteSet = spriteSet;
		this.gradient = options.color().resolve();
		this.lifetime = (int) (options.lifespan() * random.nextRange(0.9F, 1.1F));
		this.pickSprite(spriteSet);
		this.quadSize *= 6F;
		this.randomOffset = 0.8F + random.nextFloat() * 0.4F;
		var color = (gradient == null ? Color.WHITE : gradient).get(0F);
		setColor(color.redf() * options.brightness(), color.greenf() * options.brightness(), color.bluef() * options.brightness());
	}

	@Override
	public ParticleRenderType getRenderType() {
		return VidLibParticleRenderTypes.ADDITIVE;
	}

	@Override
	protected int getLightColor(float tint) {
		return 15728880;
	}

	@Override
	public void tick() {
		super.tick();

		if (age % 3 == 0) {
			this.pickSprite(spriteSet);
		}

		int decay = lifetime / 3 * 2;

		if (age > lifetime - decay) {
			alpha = 1F - (age - lifetime + decay) / (float) decay;
			quadSizeMod = KMath.lerp(relativePos, 0.25F, 1F) * options.scale() * alpha;
		} else {
			alpha = 1F;
			quadSizeMod = KMath.lerp(relativePos, 0.25F, 1F) * options.scale();
		}

		var color = (gradient == null ? Color.WHITE : gradient).get(relativeAge * randomOffset);
		setColor(color.redf() * options.brightness(), color.greenf() * options.brightness(), color.bluef() * options.brightness());
	}
}
