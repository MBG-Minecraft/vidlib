package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.kmath.KMath;
import dev.latvian.mods.kmath.render.BoxRenderer;
import dev.latvian.mods.kmath.render.BufferSupplier;
import dev.latvian.mods.kmath.vertex.VertexCallback;
import dev.latvian.mods.vidlib.feature.bloom.BloomRenderTypes;
import dev.latvian.mods.vidlib.util.Empty;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;

public class BrightCubeParticle extends CustomParticle {
	private static final BufferSupplier BLOOM = BufferSupplier.fixed(BloomRenderTypes.POS_COL.apply(Empty.TEXTURE), BloomRenderTypes.POS_COL_NO_CULL.apply(Empty.TEXTURE)).process(VertexCallback::onlyPosCol);

	private final BrightCubeParticleOptions options;

	protected BrightCubeParticle(BrightCubeParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
		super(level, x, y, z);
		this.options = options;
		setLifetime(Math.abs(options.ttl()));
	}

	@Override
	public void renderCustom(PoseStack ms, MultiBufferSource buffers, Camera camera, float delta) {
		float time = KMath.lerp(delta, prevAge, age);

		var rx = (float) (KMath.lerp(time, xo, x) - camera.getPosition().x);
		var ry = (float) (KMath.lerp(time, yo, y) - camera.getPosition().y);
		var rz = (float) (KMath.lerp(time, zo, z) - camera.getPosition().z);

		float s = options.ttl() > 0 ? 0.505F : 0.2505F;

		if (options.lineColor().alpha() > 0) {
			BoxRenderer.quads(ms, rx - s, ry, rz - s, rx + s, ry + s * 2F, rz + s, BloomRenderTypes.overridePosCol(buffers), BLOOM, true, options.lineColor());
		}

		if (options.color().alpha() > 0) {
			BoxRenderer.quads(ms, rx - s, ry, rz - s, rx + s, ry + s * 2F, rz + s, buffers, BufferSupplier.DEBUG, true, options.color());
		}
	}
}
