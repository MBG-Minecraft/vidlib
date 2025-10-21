package dev.latvian.mods.vidlib.feature.particle;

import dev.latvian.mods.klib.interpolation.Interpolation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class InterpolatedParticle extends TextureSheetParticle {
	public final Vec3 origin;
	public final Interpolation interpolation;
	public float relativeAge;
	public float relativePos;
	public float oAlpha;
	public float oQuadSizeMod;
	public float quadSizeMod;

	public InterpolatedParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, Interpolation interpolation) {
		super(level, x, y, z);
		this.interpolation = interpolation;
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

	@Override
	public ParticleRenderType getRenderType() {
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
		relativePos = interpolation.interpolateClamped(relativeAge);

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
}
