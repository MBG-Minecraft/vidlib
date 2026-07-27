package dev.latvian.mods.vidlib.feature.zone.renderer;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.render.BufferSupplier;
import dev.latvian.mods.klib.render.CuboidRenderer;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.zone.shape.JoinedZoneShape;

public class GroupZoneRenderer implements ZoneRenderer<JoinedZoneShape> {
	@Override
	public void render(JoinedZoneShape group, Context ctx) {
		if (ctx.outerBounds()) {
			var ms = ctx.frame().poseStack();
			var box = group.toAABB();
			float minX = ctx.frame().x(box.minX);
			float minY = ctx.frame().y(box.minY);
			float minZ = ctx.frame().z(box.minZ);
			float maxX = ctx.frame().x(box.maxX);
			float maxY = ctx.frame().y(box.maxY);
			float maxZ = ctx.frame().z(box.maxZ);
			CuboidRenderer.lines(ms, minX, minY, minZ, maxX, maxY, maxZ, ctx.buffers(), BufferSupplier.DEBUG_NO_DEPTH, Color.WHITE);
		}

		for (var shape : group.zoneShapes()) {
			if (ctx.frame().isVisible(shape.value().toAABB())) {
				var renderer = ZoneRenderer.get(shape.value().type());

				if (renderer != EmptyZoneRenderer.INSTANCE) {
					renderer.render(Cast.to(shape), ctx);
				}
			}
		}
	}
}
