package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.feature.zone.BlockZone;
import dev.beast.mods.shimmer.feature.zone.BoxZone;
import dev.beast.mods.shimmer.feature.zone.EmptyZone;
import dev.beast.mods.shimmer.feature.zone.UniverseZone;
import dev.beast.mods.shimmer.feature.zone.ZoneGroup;
import dev.beast.mods.shimmer.feature.zone.ZoneType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventHandler {
	@SubscribeEvent
	public static void afterLoad(FMLLoadCompleteEvent event) {
		ZoneType.register(EmptyZone.TYPE);
		ZoneType.register(UniverseZone.TYPE);
		ZoneType.register(ZoneGroup.TYPE);
		ZoneType.register(BlockZone.TYPE);
		ZoneType.register(BoxZone.TYPE);
	}
}
