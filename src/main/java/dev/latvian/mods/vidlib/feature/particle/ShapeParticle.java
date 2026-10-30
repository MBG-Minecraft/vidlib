package dev.latvian.mods.vidlib.feature.particle;

import dev.latvian.mods.klib.gradient.Gradient;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.render.DebugRenderTypes;
import dev.latvian.mods.klib.shape.Shape;
import dev.latvian.mods.vidlib.feature.bloom.BloomRenderTypes;
import dev.latvian.mods.vidlib.feature.client.RenderLightLayer;
import dev.latvian.mods.vidlib.klib.shape.Shapes;
import dev.latvian.mods.vidlib.klib.vertex.PoseTransforms;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class ShapeParticle extends CustomParticle {
	private final ShapeParticleOptions options;
	private final Ref<Shape> shape;
	private final Ref<Gradient> color;
	private final Ref<Gradient> outlineColor;

	protected ShapeParticle(ShapeParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
		super(level, x, y, z);
		this.options = options;
		this.shape = options.shape();
		this.color = options.color();
		this.outlineColor = options.outlineColor();
		setLifetime(Math.abs(options.lifespan()));
	}

	@Override
	public void submit(SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
		float time = KMath.lerp(delta, prevAge, age);
		var cameraPos = camera.position();

		ms.pushPose();
		ms.translate(
			KMath.lerp(time, xo, x) - cameraPos.x,
			KMath.lerp(time, yo, y) - cameraPos.y,
			KMath.lerp(time, zo, z) - cameraPos.z
		);

		if (options.lifespan() < 0) {
			ms.scale(0.5F, 0.5F, 0.5F);
		}

		var lc = outlineColor.value().get(time / (float) lifetime);

		if (lc.alpha() > 0) {
			if (options.lightLayer() == RenderLightLayer.BRIGHT) {
				// Bloom.markActive();
				shape.value().buildQuads(shape.value(), 0F, 0F, 0F, PoseTransforms.transform(ms.last(), BloomRenderTypes.POS_COL_BUFFER_SUPPLIER.quadsCull(BloomRenderTypes.overridePosCol(buffers))).withColor(lc.fadeOut(time, lifetime, 20F)));
			} else {
				shape.value().buildLines(ms.last().transform(buffers.getBuffer(DebugRenderTypes.LINES)).withColor(lc.fadeOut(time, lifetime, 20F)));
			}
		}

		var c = color.value().get(time / (float) lifetime);

		if (c.alpha() > 0) {
			if (options.lightLayer() == RenderLightLayer.BRIGHT) {
				// Bloom.markActive();
				Shapes.buildQuads(shape.value(), 0F, 0F, 0F, PoseTransforms.transform(ms.last(), buffers.getBuffer(DebugRenderTypes.QUADS)).withColor(c.fadeOut(time, lifetime, 20F)));
			} else {
				Shapes.buildQuads(shape.value(), 0F, 0F, 0F, PoseTransforms.transform(ms.last(), buffers.getBuffer(DebugRenderTypes.QUADS_NO_DEPTH)).withColor(c.withAlpha(50).fadeOut(time, lifetime, 20F)));
			}
		}

		ms.popPose();
	}
}
