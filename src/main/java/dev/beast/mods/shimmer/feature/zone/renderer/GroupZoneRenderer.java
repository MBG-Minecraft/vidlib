package dev.beast.mods.shimmer.feature.zone.renderer;

import dev.beast.mods.shimmer.feature.zone.ZoneShapeGroup;
import dev.beast.mods.shimmer.math.Color;
import dev.beast.mods.shimmer.util.Cast;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class GroupZoneRenderer implements ZoneRenderer<ZoneShapeGroup> {
	@Override
	public void render(ZoneShapeGroup group, Minecraft mc, RenderLevelStageEvent event, float delta, Color color, Color outlineColor) {
		for (var shape : group.zoneShapes()) {
			var renderer = ZoneRenderer.get(shape.type());

			if (renderer != EmptyZoneRenderer.INSTANCE) {
				renderer.render(Cast.to(shape), mc, event, delta, color, outlineColor);
			}
		}
	}
}
