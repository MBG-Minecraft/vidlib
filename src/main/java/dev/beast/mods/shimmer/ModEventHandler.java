package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.feature.camerashake.CameraShakeType;
import dev.beast.mods.shimmer.feature.entity.filter.EntityFilter;
import dev.beast.mods.shimmer.feature.zone.ZoneShape;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumber;
import dev.beast.mods.shimmer.math.worldposition.WorldPosition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventHandler {
	@SubscribeEvent
	public static void afterLoad(FMLLoadCompleteEvent event) {
		WorldNumber.bootstrap();
		WorldPosition.bootstrap();
		EntityFilter.bootstrap();
		ZoneShape.bootstrap();
		CameraShakeType.bootstrap();
	}

	@SubscribeEvent
	public static void buildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.OP_BLOCKS) {
			for (var item : BuiltInRegistries.ITEM) {
				if (item.builtInRegistryHolder().getKey().location().getNamespace().equals(Shimmer.ID)) {
					event.accept(item);
				}
			}
		}
	}
}
