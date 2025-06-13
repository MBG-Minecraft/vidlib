package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.render.DebugRenderTypes;
import dev.latvian.mods.klib.shape.Shape;
import dev.latvian.mods.vidlib.feature.bloom.BloomRenderTypes;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;

public class ShapeParticle extends CustomParticle {
	private final ShapeParticleOptions options;
	private final Shape shape;
	private final Gradient color;
	private final Gradient outlineColor;

	protected ShapeParticle(ShapeParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
		super(level, x, y, z);
		this.options = options;
		this.shape = options.shape();
		this.color = options.color().resolve();
		this.outlineColor = options.outlineColor().resolve();
		setLifetime(Math.abs(options.lifespan()));
	}

	@Override
	public void renderCustom(PoseStack ms, MultiBufferSource buffers, Camera camera, float delta) {
		float time = KMath.lerp(delta, prevAge, age);
		var cameraPos = camera.getPosition();

		ms.pushPose();
		ms.translate(
			KMath.lerp(time, xo, x) - cameraPos.x,
			KMath.lerp(time, yo, y) - cameraPos.y,
			KMath.lerp(time, zo, z) - cameraPos.z
		);

		if (options.lifespan() < 0) {
			ms.scale(0.5F, 0.5F, 0.5F);
		}

		var lc = outlineColor.get(time / (float) lifetime);

		if (lc.alpha() > 0) {
			if (options.bright()) {
				shape.buildQuads(0F, 0F, 0F, ms.last().transform(BloomRenderTypes.POS_COL_BUFFER_SUPPLIER.quadsCull(BloomRenderTypes.overridePosCol(buffers))).withColor(lc.fadeOut(time, lifetime, 20F)));
			} else {
				shape.buildLines(0F, 0F, 0F, ms.last().transform(buffers.getBuffer(DebugRenderTypes.LINES)).withColor(lc.fadeOut(time, lifetime, 20F)));
			}
		}

		var c = color.get(time / (float) lifetime);

		if (c.alpha() > 0) {
			if (options.bright()) {
				shape.buildQuads(0F, 0F, 0F, ms.last().transform(buffers.getBuffer(DebugRenderTypes.QUADS)).withColor(c.fadeOut(time, lifetime, 20F)));
			} else {
				shape.buildQuads(0F, 0F, 0F, ms.last().transform(buffers.getBuffer(DebugRenderTypes.QUADS_NO_DEPTH)).withColor(c.withAlpha(50).fadeOut(time, lifetime, 20F)));
			}
		}

		ms.popPose();
	}
}
