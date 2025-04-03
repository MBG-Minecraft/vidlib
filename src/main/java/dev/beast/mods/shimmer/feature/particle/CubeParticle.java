package dev.beast.mods.shimmer.feature.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.beast.mods.shimmer.math.BoxRenderer;
import dev.beast.mods.shimmer.math.KMath;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;

public class CubeParticle extends CustomParticle {
	private final CubeParticleOptions options;

	protected CubeParticle(CubeParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
		super(level, x, y, z);
		this.options = options;
		setLifetime(Math.abs(options.ttl()));
	}

	@Override
	public void renderCustom(PoseStack ms, MultiBufferSource buffers, Camera camera, float delta) {
		float time = KMath.lerp(delta, prevAge, age);

		var rx = KMath.lerp(time, xo, x);
		var ry = KMath.lerp(time, yo, y);
		var rz = KMath.lerp(time, zo, z);

		var cameraPos = camera.getPosition();
		double size = options.ttl() > 0 ? 0.505D : 0.2505D;

		float minX = (float) (rx - size - cameraPos.x);
		float minY = (float) (ry - size - cameraPos.y);
		float minZ = (float) (rz - size - cameraPos.z);
		float maxX = (float) (rx + size - cameraPos.x);
		float maxY = (float) (ry + size - cameraPos.y);
		float maxZ = (float) (rz + size - cameraPos.z);

		if (options.lineColor().alpha() > 0) {
			BoxRenderer.renderDebugLines(minX, minY, minZ, maxX, maxY, maxZ, ms, buffers, options.lineColor().fadeOut(time, lifetime, 20F));
		}

		if (options.color().alpha() > 0) {
			BoxRenderer.renderDebugQuads(minX, minY, minZ, maxX, maxY, maxZ, ms, buffers, false, options.color().withAlpha(50).fadeOut(time, lifetime, 20F));
		}
	}
}
