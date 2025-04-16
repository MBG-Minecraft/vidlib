package dev.latvian.mods.vidlib.feature.zone.renderer;

import dev.latvian.mods.kmath.SpherePoints;
import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.kmath.render.BoxRenderer;
import dev.latvian.mods.kmath.render.SphereRenderer;
import dev.latvian.mods.vidlib.feature.zone.shape.SphereZoneShape;

public class SphereZoneRenderer implements ZoneRenderer<SphereZoneShape> {
	public static final SphereZoneRenderer INSTANCE = new SphereZoneRenderer();

	@Override
	public void render(SphereZoneShape shape, Context ctx) {
		var ms = ctx.frame().poseStack();

		var box = shape.getBoundingBox();
		float minX = ctx.frame().x(box.minX);
		float minY = ctx.frame().y(box.minY);
		float minZ = ctx.frame().z(box.minZ);
		float maxX = ctx.frame().x(box.maxX);
		float maxY = ctx.frame().y(box.maxY);
		float maxZ = ctx.frame().z(box.maxZ);
		BoxRenderer.renderDebugLines(minX, minY, minZ, maxX, maxY, maxZ, ms, ctx.buffers(), Color.WHITE);

		ms.pushPose();
		ctx.frame().translate(shape.pos());
		float scale = (float) (shape.radius() * 2D);
		ms.scale(scale, scale, scale);
		SphereRenderer.renderDebugLines(SpherePoints.M, ms, ctx.buffers(), ctx.outlineColor());
		SphereRenderer.renderDebugQuads(SpherePoints.M, ms, ctx.buffers(), false, ctx.color());
		ms.popPose();
	}
}
