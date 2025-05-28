package dev.latvian.mods.vidlib.feature.zone.renderer;

import dev.latvian.mods.kmath.render.BoxRenderer;
import dev.latvian.mods.kmath.render.BufferSupplier;
import dev.latvian.mods.vidlib.feature.zone.shape.ZoneShape;

public class BoxZoneRenderer implements ZoneRenderer<ZoneShape> {
	public static final BoxZoneRenderer INSTANCE = new BoxZoneRenderer();

	@Override
	public void render(ZoneShape shape, Context ctx) {
		var ms = ctx.frame().poseStack();
		var box = shape.getBoundingBox();

		float minX = ctx.frame().x(box.minX);
		float minY = ctx.frame().y(box.minY);
		float minZ = ctx.frame().z(box.minZ);
		float maxX = ctx.frame().x(box.maxX);
		float maxY = ctx.frame().y(box.maxY);
		float maxZ = ctx.frame().z(box.maxZ);

		BoxRenderer.lines(ms, minX, minY, minZ, maxX, maxY, maxZ, ctx.buffers(), BufferSupplier.DEBUG_NO_DEPTH, ctx.outlineColor());
		BoxRenderer.quads(ms, minX, minY, minZ, maxX, maxY, maxZ, ctx.buffers(), BufferSupplier.DEBUG_NO_DEPTH, false, ctx.color());
	}
}
