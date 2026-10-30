package dev.latvian.mods.vidlib.feature.zone.renderer;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.render.DebugRenderTypes;
import dev.latvian.mods.vidlib.feature.zone.shape.CylinderZoneShape;
import dev.latvian.mods.vidlib.klib.render.BufferSupplier;
import dev.latvian.mods.vidlib.klib.render.CuboidRenderer;
import dev.latvian.mods.vidlib.klib.shape.Shapes;
import dev.latvian.mods.vidlib.klib.vertex.PoseTransforms;

public class CylinderZoneRenderer implements ZoneRenderer<CylinderZoneShape> {
	@Override
	public void render(CylinderZoneShape shape, Context ctx) {
		var ms = ctx.frame().poseStack();

		if (ctx.outerBounds()) {
			var box = shape.toAABB();
			float minX = ctx.frame().x(box.minX);
			float minY = ctx.frame().y(box.minY);
			float minZ = ctx.frame().z(box.minZ);
			float maxX = ctx.frame().x(box.maxX);
			float maxY = ctx.frame().y(box.maxY);
			float maxZ = ctx.frame().z(box.maxZ);
			CuboidRenderer.lines(ms, minX, minY, minZ, maxX, maxY, maxZ, ctx.buffers(), BufferSupplier.DEBUG_NO_DEPTH, Color.WHITE);
		}

		ms.pushPose();
		ctx.frame().translate(shape.pos());
		Shapes.buildLines(shape.shape(), 0F, 0F, 0F, PoseTransforms.transform(ms.last(), ctx.buffers().getBuffer(DebugRenderTypes.LINES)).withColor(ctx.outlineColor()));
		Shapes.buildQuads(shape.shape(), 0F, 0F, 0F, PoseTransforms.transform(ms.last(), ctx.buffers().getBuffer(DebugRenderTypes.QUADS_NO_CULL_NO_DEPTH)).withColor(ctx.color()));
		ms.popPose();
	}
}
