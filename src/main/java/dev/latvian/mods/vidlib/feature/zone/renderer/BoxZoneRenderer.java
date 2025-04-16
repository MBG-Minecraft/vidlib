package dev.latvian.mods.vidlib.feature.zone.renderer;

import dev.latvian.mods.kmath.render.BoxRenderer;
import dev.latvian.mods.vidlib.feature.zone.shape.ZoneShape;

public class BoxZoneRenderer implements ZoneRenderer<ZoneShape> {
	public static final BoxZoneRenderer INSTANCE = new BoxZoneRenderer();

	@Override
	public void render(ZoneShape shape, Context ctx) {
		var ms = ctx.frame().poseStack();
		var box = shape.getBoundingBox();
		float minX = (float) (box.minX - ctx.frame().cameraX());
		float minY = (float) (box.minY - ctx.frame().cameraY());
		float minZ = (float) (box.minZ - ctx.frame().cameraZ());
		float maxX = (float) (box.maxX - ctx.frame().cameraX());
		float maxY = (float) (box.maxY - ctx.frame().cameraY());
		float maxZ = (float) (box.maxZ - ctx.frame().cameraZ());

		BoxRenderer.renderDebugLines(minX, minY, minZ, maxX, maxY, maxZ, ms, ctx.buffers(), ctx.outlineColor());
		BoxRenderer.renderDebugQuads(minX, minY, minZ, maxX, maxY, maxZ, ms, ctx.buffers(), false, ctx.color());
	}
}
