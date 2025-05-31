package dev.latvian.mods.vidlib.feature.zone.renderer;

import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.kmath.render.BufferSupplier;
import dev.latvian.mods.kmath.render.CuboidRenderer;
import dev.latvian.mods.kmath.render.SphereRenderer;
import dev.latvian.mods.kmath.shape.SpherePoints;
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
		CuboidRenderer.lines(ms, minX, minY, minZ, maxX, maxY, maxZ, ctx.buffers(), BufferSupplier.DEBUG_NO_DEPTH, Color.WHITE);

		ms.pushPose();
		ctx.frame().translate(shape.pos());
		float scale = (float) (shape.radius() * 2D);
		SphereRenderer.lines(ms, 0F, 0F, 0F, scale, SpherePoints.M, ctx.buffers(), BufferSupplier.DEBUG_NO_DEPTH, ctx.outlineColor());
		SphereRenderer.quads(ms, 0F, 0F, 0F, scale, SpherePoints.M, ctx.buffers(), BufferSupplier.DEBUG_NO_DEPTH, false, ctx.color());
		ms.popPose();
	}
}
