package dev.beast.mods.shimmer.feature.zone.renderer;

import dev.beast.mods.shimmer.feature.zone.shape.ZoneShapeGroup;
import dev.beast.mods.shimmer.util.Cast;

public class GroupZoneRenderer implements ZoneRenderer<ZoneShapeGroup> {
	@Override
	public void render(ZoneShapeGroup group, Context ctx) {
		for (var shape : group.zoneShapes()) {
			if (ctx.frame().frustum().isVisible(shape.getBoundingBox())) {
				var renderer = ZoneRenderer.get(shape.type());

				if (renderer != EmptyZoneRenderer.INSTANCE) {
					renderer.render(Cast.to(shape), ctx);
				}
			}
		}
	}
}
