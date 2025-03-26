package dev.beast.mods.shimmer.feature.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.beast.mods.shimmer.math.BoxRenderer;
import dev.beast.mods.shimmer.math.KMath;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;

public class CubeParticle extends Particle {
	private int prevAge;
	private final CubeParticleOptions options;

	protected CubeParticle(CubeParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
		super(level, x, y, z);
		this.options = options;
		setSize(1F, 1F);
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
			int alpha = time > (lifetime - 20) ? Mth.lerpInt(1F - (lifetime - time) / 20F, 255, 0) : 255;
			BoxRenderer.renderDebugLines(minX, minY, minZ, maxX, maxY, maxZ, ms, buffers, options.lineColor().withAlpha(alpha));
		}

		if (options.color().alpha() > 0) {
			int alpha = time > (lifetime - 20) ? Mth.lerpInt(1F - (lifetime - time) / 20F, 50, 0) : 50;
			BoxRenderer.renderDebugQuads(minX, minY, minZ, maxX, maxY, maxZ, ms, buffers, false, options.color().withAlpha(alpha));
		}
	}

	@Override
	public void render(VertexConsumer buffer, Camera camera, float delta) {
	}

	@Override
	public void tick() {
		prevAge = age;
		super.tick();
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.CUSTOM;
	}
}
