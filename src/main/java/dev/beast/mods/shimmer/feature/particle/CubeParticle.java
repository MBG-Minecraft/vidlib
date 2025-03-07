package dev.beast.mods.shimmer.feature.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.beast.mods.shimmer.math.BoxRenderer;
import dev.beast.mods.shimmer.math.KMath;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.util.Mth;

public class CubeParticle extends Particle {
	private int prevAge;
	private final CubeParticleOptions options;

	protected CubeParticle(CubeParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
		super(level, x, y, z);
		this.options = options;
		setSize(1F, 1F);
		setLifetime(options.lifetime());
	}

	@Override
	public void render(VertexConsumer buffer, Camera camera, float delta) {
		float time = KMath.lerp(delta, prevAge, age);

		var rx = KMath.lerp(time, xo, x);
		var ry = KMath.lerp(time, yo, y);
		var rz = KMath.lerp(time, zo, z);

		var cameraPos = camera.getPosition();
		float minX = (float) (rx - 0.505D - cameraPos.x);
		float minY = (float) (ry - 0.505D - cameraPos.y);
		float minZ = (float) (rz - 0.505D - cameraPos.z);
		float maxX = (float) (rx + 0.505D - cameraPos.x);
		float maxY = (float) (ry + 0.505D - cameraPos.y);
		float maxZ = (float) (rz + 0.505D - cameraPos.z);

		var mc = Minecraft.getInstance();
		var ms = new PoseStack();

		int alpha = time > (options.lifetime() - 20) ? Mth.lerpInt(1F - (options.lifetime() - time) / 20F, 50, 0) : 50;

		if (options.lineColor().argb() != 0) {
			BoxRenderer.renderDebugLines(minX, minY, minZ, maxX, maxY, maxZ, ms, mc.renderBuffers().bufferSource(), options.lineColor());
		}

		BoxRenderer.renderDebugQuads(minX, minY, minZ, maxX, maxY, maxZ, ms, mc.renderBuffers().bufferSource(), false, options.color().withAlpha(alpha));
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
