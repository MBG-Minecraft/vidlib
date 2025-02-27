package dev.beast.mods.shimmer.feature.zone.renderer;

import dev.beast.mods.shimmer.feature.zone.ZoneShape;
import dev.beast.mods.shimmer.math.Color;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class EmptyZoneRenderer implements ZoneRenderer<ZoneShape> {
	public static final EmptyZoneRenderer INSTANCE = new EmptyZoneRenderer();

	@Override
	public void render(ZoneShape shape, Minecraft mc, RenderLevelStageEvent event, Color color, Color outlineColor) {
	}
}
