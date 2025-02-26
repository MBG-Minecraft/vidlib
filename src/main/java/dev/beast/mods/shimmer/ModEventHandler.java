package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.feature.zone.BlockZoneShape;
import dev.beast.mods.shimmer.feature.zone.BoxZoneShape;
import dev.beast.mods.shimmer.feature.zone.EmptyZoneShape;
import dev.beast.mods.shimmer.feature.zone.UniverseZoneShape;
import dev.beast.mods.shimmer.feature.zone.ZoneShapeGroup;
import dev.beast.mods.shimmer.feature.zone.ZoneShapeType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventHandler {
	@SubscribeEvent
	public static void afterLoad(FMLLoadCompleteEvent event) {
		ZoneShapeType.register(EmptyZoneShape.TYPE);
		ZoneShapeType.register(UniverseZoneShape.TYPE);
		ZoneShapeType.register(ZoneShapeGroup.TYPE);
		ZoneShapeType.register(BlockZoneShape.TYPE);
		ZoneShapeType.register(BoxZoneShape.TYPE);
	}
}
