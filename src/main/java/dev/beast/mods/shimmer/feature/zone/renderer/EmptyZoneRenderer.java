package dev.beast.mods.shimmer.feature.zone.renderer;

import dev.beast.mods.shimmer.feature.zone.ZoneInstance;
import dev.beast.mods.shimmer.feature.zone.ZoneShape;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class EmptyZoneRenderer implements ZoneRenderer<ZoneShape> {
	public static final EmptyZoneRenderer INSTANCE = new EmptyZoneRenderer();

	@Override
	public void render(ZoneShape shape, ZoneInstance instance, Minecraft mc, RenderLevelStageEvent event) {
	}
}
