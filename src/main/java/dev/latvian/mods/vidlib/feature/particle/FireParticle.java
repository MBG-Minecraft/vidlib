package dev.latvian.mods.vidlib.feature.particle;

import dev.latvian.mods.klib.gradient.Gradient;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.registry.Ref;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.Mth;

public class FireParticle extends InterpolatedParticle {
	public static ParticleProvider<FireParticleOptions> create(SpriteSet spriteSet) {
		return (options, level, x, y, z, xd, yd, zd, random) -> new FireParticle(options, level, x, y, z, xd, yd, zd, spriteSet);
	}

	public final FireParticleOptions options;
	public final SpriteSet spriteSet;
	public final Ref<Gradient> gradient;
	public final float randomOffset;

	public FireParticle(FireParticleOptions options, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet spriteSet) {
		super(level, x, y, z, xd, yd, zd, options.ease());
		this.options = options;
		this.spriteSet = spriteSet;
		this.gradient = options.color();
		this.lifetime = (int) (options.lifespan() * Mth.lerp(random.nextFloat(), 0.9F, 1.1F));
		this.pickSprite(spriteSet);
		this.quadSize *= 6F;
		this.randomOffset = 0.8F + random.nextFloat() * 0.4F;
		var color = gradient.value().get(0F);
		setColor(color.redf() * options.brightness(), color.greenf() * options.brightness(), color.bluef() * options.brightness());
	}

	@Override
	protected SingleQuadParticle.Layer getParticleLayer() {
		return VidLibParticleRenderTypes.ADDITIVE_LAYER;
	}

	@Override
	public ParticleRenderType getGroup() {
		return VidLibParticleRenderTypes.ADDITIVE;
	}

	@Override
	protected int getLightCoords(float partialTick) {
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

		var color = gradient.value().get(relativeAge * randomOffset);
		setColor(color.redf() * options.brightness(), color.greenf() * options.brightness(), color.bluef() * options.brightness());
	}
}
