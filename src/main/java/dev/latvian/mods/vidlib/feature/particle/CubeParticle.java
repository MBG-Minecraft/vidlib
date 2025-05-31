package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.kmath.KMath;
import dev.latvian.mods.kmath.color.Gradient;
import dev.latvian.mods.kmath.render.BufferSupplier;
import dev.latvian.mods.kmath.render.CuboidRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;

public class CubeParticle extends CustomParticle {
	private final CubeParticleOptions options;
	private final Gradient color;
	private final Gradient lineColor;

	protected CubeParticle(CubeParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
		super(level, x, y, z);
		this.options = options;
		this.color = options.color().resolve();
		this.lineColor = options.lineColor().resolve();
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

		var lc = lineColor.get(time / (float) lifetime);

		if (lc.alpha() > 0) {
			CuboidRenderer.lines(ms, minX, minY, minZ, maxX, maxY, maxZ, buffers, BufferSupplier.DEBUG_NO_DEPTH, lc.fadeOut(time, lifetime, 20F));
		}

		var c = color.get(time / (float) lifetime);

		if (c.alpha() > 0) {
			CuboidRenderer.quads(ms, minX, minY, minZ, maxX, maxY, maxZ, buffers, BufferSupplier.DEBUG_NO_DEPTH, false, c.withAlpha(50).fadeOut(time, lifetime, 20F));
		}
	}
}
