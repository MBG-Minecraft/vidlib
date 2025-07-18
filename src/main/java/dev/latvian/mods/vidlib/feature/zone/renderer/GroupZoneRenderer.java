package dev.latvian.mods.vidlib.feature.zone.renderer;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.render.BufferSupplier;
import dev.latvian.mods.klib.render.CuboidRenderer;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.zone.shape.ZoneShapeGroup;

public class GroupZoneRenderer implements ZoneRenderer<ZoneShapeGroup> {
	@Override
	public void render(ZoneShapeGroup group, Context ctx) {
		if (ctx.outerBounds()) {
			var ms = ctx.frame().poseStack();
			var box = group.getBoundingBox();
			float minX = ctx.frame().x(box.minX);
			float minY = ctx.frame().y(box.minY);
			float minZ = ctx.frame().z(box.minZ);
			float maxX = ctx.frame().x(box.maxX);
			float maxY = ctx.frame().y(box.maxY);
			float maxZ = ctx.frame().z(box.maxZ);
			CuboidRenderer.lines(ms, minX, minY, minZ, maxX, maxY, maxZ, ctx.buffers(), BufferSupplier.DEBUG_NO_DEPTH, Color.WHITE);
		}

		for (var shape : group.zoneShapes()) {
			if (ctx.frame().isVisible(shape.getBoundingBox())) {
				var renderer = ZoneRenderer.get(shape.type());

				if (renderer != EmptyZoneRenderer.INSTANCE) {
					renderer.render(Cast.to(shape), ctx);
				}
			}
		}
	}
}
