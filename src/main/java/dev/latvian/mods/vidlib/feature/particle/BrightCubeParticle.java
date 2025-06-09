package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.render.BufferSupplier;
import dev.latvian.mods.klib.render.CuboidRenderer;
import dev.latvian.mods.vidlib.feature.bloom.BloomRenderTypes;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;

public class BrightCubeParticle extends CustomParticle {
	private final BrightCubeParticleOptions options;
	private final Gradient color;
	private final Gradient lineColor;

	protected BrightCubeParticle(BrightCubeParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
		super(level, x, y, z);
		this.options = options;
		this.color = options.color().resolve();
		this.lineColor = options.lineColor().resolve();
		setLifetime(Math.abs(options.ttl()));
	}

	@Override
	public void renderCustom(PoseStack ms, MultiBufferSource buffers, Camera camera, float delta) {
		float time = KMath.lerp(delta, prevAge, age);

		var rx = (float) (KMath.lerp(time, xo, x) - camera.getPosition().x);
		var ry = (float) (KMath.lerp(time, yo, y) - camera.getPosition().y);
		var rz = (float) (KMath.lerp(time, zo, z) - camera.getPosition().z);

		float s = options.ttl() > 0 ? 0.505F : 0.2505F;

		var lc = lineColor.get(time / (float) lifetime);

		if (lc.alpha() > 0) {
			CuboidRenderer.quads(ms, rx - s, ry, rz - s, rx + s, ry + s * 2F, rz + s, BloomRenderTypes.overridePosCol(buffers), BloomRenderTypes.POS_COL_BUFFER_SUPPLIER, true, lc);
		}

		var c = color.get(time / (float) lifetime);

		if (c.alpha() > 0) {
			CuboidRenderer.quads(ms, rx - s, ry, rz - s, rx + s, ry + s * 2F, rz + s, buffers, BufferSupplier.DEBUG, true, c);
		}
	}
}
