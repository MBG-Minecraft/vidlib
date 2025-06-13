package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.klib.math.KMath;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;

public class ItemParticle extends CustomParticle {
	private final ItemParticleOptions options;

	protected ItemParticle(ItemParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
		super(level, x, y, z, vx, vy, vz);
		this.options = options;
		setLifetime(Math.abs(options.lifespan()));
	}

	@Override
	public void renderCustom(PoseStack ms, MultiBufferSource buffers, Camera camera, float delta) {
		float time = KMath.lerp(delta, prevAge, age);

		if (time >= lifetime - 1F) {
			return;
		}

		var cameraPos = camera.getPosition();
		var rx = (float) (KMath.lerp(delta, xo, x) - cameraPos.x);
		var ry = (float) (KMath.lerp(delta, yo, y) - cameraPos.y);
		var rz = (float) (KMath.lerp(delta, zo, z) - cameraPos.z);

		ms.pushPose();
		ms.translate(rx, ry, rz);
		// FIXME
		ms.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(float partialTicks) {
		return AABB.INFINITE;
	}
}
