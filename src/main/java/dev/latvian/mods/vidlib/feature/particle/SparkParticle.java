package dev.latvian.mods.vidlib.feature.particle;

import dev.latvian.mods.klib.gradient.Gradient;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.gradient.ClientGradients;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.joml.Math;

public class SparkParticle extends SingleQuadParticle implements DepthPassQuadParticle {
	public static ParticleProvider<VidLibParticles.SimpleParticleType> create(SpriteSet spriteSet) {
		return (type, level, x, y, z, xd, yd, zd, random) -> new SparkParticle(level, x, y, z, xd, yd, zd, spriteSet);
	}

	private final SpriteSet spriteSet;
	private final Ref<Gradient> gradient;
	private final float randomOffset;
	private float oQuadSize;
	private SingleQuadParticle.Layer forcedLayer;

	public SparkParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet spriteSet) {
		super(level, x, y, z, (TextureAtlasSprite) null);
		this.spriteSet = spriteSet;
		this.gradient = ClientGradients.SPARK;
		this.pickSprite(spriteSet);
		this.xd = xd;
		this.yd = yd;
		this.zd = zd;
		this.friction = 0.9F;
		this.randomOffset = random.nextFloat();
		this.lifetime = (int) KMath.lerp(randomOffset, 15, org.joml.Math.max(30, Math.sqrt(xd * xd + yd * yd + zd * zd) * 10));
		this.gravity = KMath.lerp(randomOffset, 1.1F, 1.5F);
		this.oQuadSize = quadSize;
	}

	/**
	 * 26.3's {@link SingleQuadParticle} dropped {@code TextureSheetParticle#pickSprite}.
	 */
	public void pickSprite(SpriteSet sprites) {
		this.setSprite(sprites.get(random));
	}

	@Override
	protected SingleQuadParticle.Layer getLayer() {
		return forcedLayer == null ? VidLibParticleRenderTypes.ADDITIVE_LAYER : forcedLayer;
	}

	@Override
	public void vl$setForcedLayer(SingleQuadParticle.Layer layer) {
		this.forcedLayer = layer;
	}

	@Override
	public ParticleRenderType getGroup() {
		return VidLibParticleRenderTypes.ADDITIVE;
	}

	@Override
	public void tick() {
		oQuadSize = quadSize;
		hasPhysics = yd < 0D;
		oRoll = roll;
		super.tick();
		setSpriteFromAge(spriteSet);
		var col = gradient.value().get(age / (float) lifetime + KMath.lerp(randomOffset, -0.3F, 0.3F));
		setColor(col.redf(), col.greenf(), col.bluef());
		alpha = 1F - (age / (float) lifetime);
		quadSize = alpha * 0.2F;
		roll = KMath.lerp(randomOffset, -1F, 1F) * alpha;
	}

	@Override
	protected int getLightCoords(float partialTick) {
		return 15728880;
	}

	@Override
	public float getQuadSize(float delta) {
		return KMath.lerp(delta, oQuadSize, quadSize);
	}
}
