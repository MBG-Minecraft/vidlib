package dev.beast.mods.shimmer.feature.zone.renderer;

import dev.beast.mods.shimmer.feature.zone.Zone;
import dev.beast.mods.shimmer.feature.zone.ZoneInstance;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class EmptyZoneRenderer implements ZoneRenderer<Zone> {
	public static final EmptyZoneRenderer INSTANCE = new EmptyZoneRenderer();

	@Override
	public void render(Zone zone, ZoneInstance instance, Minecraft mc, RenderLevelStageEvent event) {
	}
}
