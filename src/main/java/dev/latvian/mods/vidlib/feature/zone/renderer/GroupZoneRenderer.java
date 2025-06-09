package dev.latvian.mods.vidlib.feature.zone.renderer;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.zone.shape.ZoneShapeGroup;

public class GroupZoneRenderer implements ZoneRenderer<ZoneShapeGroup> {
	@Override
	public void render(ZoneShapeGroup group, Context ctx) {
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
