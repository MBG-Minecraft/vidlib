package dev.beast.mods.shimmer.feature.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.beast.mods.shimmer.math.Easing;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class TargetedParticle extends TextureSheetParticle {
	public final Vec3 origin;
	public final Vec3 target;
	public final Easing easing;
	public int oAge;
	public float oAlpha;
	public float oQuadSizeMod;
	public float quadSizeMod;

	public TargetedParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, Easing easing) {
		super(level, x, y, z);
		this.easing = easing;
		this.xd = 0D;
		this.yd = 0D;
		this.zd = 0D;
		this.friction = 1F;
		this.gravity = 0F;
		this.oRoll = this.roll = (float) (random.nextFloat() * Math.PI * 2D);
		this.origin = new Vec3(x, y, z);
		this.target = new Vec3(x + xd, y + yd, z + zd);
		this.hasPhysics = false;
		this.rCol = this.gCol = this.bCol = 1F;
		this.oQuadSizeMod = this.quadSizeMod = 0F;
		this.oAlpha = this.alpha = 0F;
		this.oAge = 0;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ShimmerParticleRenderTypes.TRUE_TRANSLUCENT;
	}

	@Override
	protected void renderRotatedQuad(VertexConsumer buffer, Camera camera, Quaternionf quaternion, float delta) {
		var cam = camera.getPosition();
		double a = easing.ease(Mth.lerp(delta, oAge, age) / (double) lifetime);
		float rx = (float) (Mth.lerp(a, origin.x, target.x) - cam.x);
		float ry = (float) (Mth.lerp(a, origin.y, target.y) - cam.y);
		float rz = (float) (Mth.lerp(a, origin.z, target.z) - cam.z);
		renderRotatedQuad(buffer, quaternion, rx, ry, rz, delta);
	}

	@Override
	public void tick() {
		oAge = age;
		oQuadSizeMod = quadSizeMod;
		oRoll = roll;
		oAlpha = alpha;
		hasPhysics = yd < 0D;
		xd = 0D;
		yd = 0D;
		zd = 0D;
		xo = x;
		yo = y;
		zo = z;

		double a = easing.ease(age / (double) lifetime);

		setPos(
			Mth.lerp(a, origin.x, target.x),
			Mth.lerp(a, origin.y, target.y),
			Mth.lerp(a, origin.z, target.z)
		);

		if (a >= 0.8D) {
			alpha = (float) (1D - (a - 0.8D) / 0.2D) * 0.8F;
		} else {
			alpha = 0.2F;
		}

		if (a >= 0.8D) {
			quadSizeMod = (float) Mth.lerp((a - 0.8D) / 0.2D, 1D, 2D);
		} else {
			quadSizeMod = 1F;
		}

		if (age++ >= lifetime) {
			remove();
		}
	}

	@Override
	public float getQuadSize(float delta) {
		return super.getQuadSize(delta) * Mth.lerp(delta, oQuadSizeMod, quadSizeMod);
	}
}
