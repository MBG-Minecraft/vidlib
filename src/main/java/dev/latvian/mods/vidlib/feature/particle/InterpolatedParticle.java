package dev.latvian.mods.vidlib.feature.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EasingType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * 26.3 port: {@code TextureSheetParticle} was replaced by {@link SingleQuadParticle}, which no
 * longer owns the size/colour/roll fields - they move into the render state via
 * {@code QuadParticleRenderState.add(...)} - but they are still declared here so the mod's existing
 * {@code quadSizeMod}/{@code alpha}/{@code rCol} maths keeps working verbatim.
 */
public class InterpolatedParticle extends SingleQuadParticle implements DepthPassQuadParticle {
	public final Vec3 origin;
	public final EasingType ease;
	public float relativeAge;
	public float relativePos;
	public float oAlpha;
	public float oQuadSizeMod;
	public float quadSizeMod;
	private SingleQuadParticle.Layer forcedLayer;

	public InterpolatedParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, EasingType interpolation) {
		super(level, x, y, z, (TextureAtlasSprite) null);
		this.ease = interpolation;
		this.xd = xd;
		this.yd = yd;
		this.zd = zd;
		this.friction = 1F;
		this.gravity = 0F;
		this.oRoll = this.roll = (float) (random.nextFloat() * Math.PI * 2D);
		this.origin = new Vec3(x, y, z);
		this.hasPhysics = false;
		this.rCol = this.gCol = this.bCol = 1F;
		this.oQuadSizeMod = this.quadSizeMod = 0F;
		this.oAlpha = this.alpha = 0F;
		this.relativeAge = 0F;
		this.relativePos = 0F;
	}

	/**
	 * 26.3's {@link SingleQuadParticle} dropped {@code TextureSheetParticle#pickSprite}.
	 */
	public void pickSprite(SpriteSet sprites) {
		this.setSprite(sprites.get(random));
	}

	/**
	 * The layer this particle's quads are batched into. Overridden by subclasses that use a
	 * different pipeline.
	 */
	protected SingleQuadParticle.Layer getParticleLayer() {
		return VidLibParticleRenderTypes.TRUE_TRANSLUCENT_LAYER;
	}

	@Override
	protected SingleQuadParticle.Layer getLayer() {
		return forcedLayer == null ? getParticleLayer() : forcedLayer;
	}

	@Override
	public void vl$setForcedLayer(SingleQuadParticle.Layer layer) {
		this.forcedLayer = layer;
	}

	@Override
	public ParticleRenderType getGroup() {
		return VidLibParticleRenderTypes.TRUE_TRANSLUCENT;
	}

	@Override
	public void tick() {
		oQuadSizeMod = quadSizeMod;
		oRoll = roll;
		oAlpha = alpha;
		xo = x;
		yo = y;
		zo = z;

		relativeAge = age / (float) lifetime;
		relativePos = Math.clamp(ease.apply(relativeAge), 0F, 1F);

		setPos(
			origin.x + xd * relativePos,
			origin.y + yd * relativePos,
			origin.z + zd * relativePos
		);

		if (relativePos >= 0.8F) {
			alpha = (1F - (relativePos - 0.8F) / 0.2F) * 0.8F;
		} else {
			alpha = 0.2F;
		}

		if (relativePos == 0F) {
			quadSizeMod = 0F;
		} else if (relativePos >= 0.8F) {
			quadSizeMod = Mth.lerp((relativePos - 0.8F) / 0.2F, 1F, 2F);
		} else {
			quadSizeMod = 1F;
		}

		if (age++ >= lifetime) {
			remove();
		}
	}

	@Override
	public void move(double x, double y, double z) {
		setBoundingBox(getBoundingBox().move(x, y, z));
		setLocationFromBoundingbox();
	}

	@Override
	public float getQuadSize(float delta) {
		return super.getQuadSize(delta) * Mth.lerp(delta, oQuadSizeMod, quadSizeMod);
	}

	/**
	 * Subclasses pick their sprite after construction, so the quad bounds have to tolerate a
	 * missing sprite rather than crash the render thread.
	 */
	@Override
	protected float getU0() {
		return sprite == null ? 0F : super.getU0();
	}

	@Override
	protected float getU1() {
		return sprite == null ? 1F : super.getU1();
	}

	@Override
	protected float getV0() {
		return sprite == null ? 0F : super.getV0();
	}

	@Override
	protected float getV1() {
		return sprite == null ? 1F : super.getV1();
	}
}
