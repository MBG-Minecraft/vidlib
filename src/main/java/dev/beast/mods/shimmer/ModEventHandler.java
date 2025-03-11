package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.core.ShimmerPayloadRegistrar;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.toolitem.ToolItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.modscan.ModAnnotation;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.lang.annotation.ElementType;
import java.util.List;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventHandler {
	@SubscribeEvent
	public static void afterLoad(FMLLoadCompleteEvent event) {
		var classLoader = AutoInit.class.getModule().getClassLoader();

		for (var scan : ModList.get().getAllScanData()) {
			scan.getAnnotatedBy(AutoInit.class, ElementType.TYPE).forEach(ad -> {
				var distData = ad.annotationData().get("value");

				if (distData != null) {
					var list = (List<ModAnnotation.EnumHolder>) distData;

					if (!list.stream().map(h -> Dist.valueOf(h.value())).toList().contains(FMLLoader.getDist())) {
						Shimmer.LOGGER.info("Skipped @AutoInit class " + ad.clazz().getClassName());
						return;
					}
				}

				try {
					var type = Class.forName(ad.clazz().getClassName(), true, classLoader);
					Shimmer.LOGGER.info("Initialized @AutoInit class " + type.getName());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
		}
	}

	@SubscribeEvent
	public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
		var reg = ShimmerPayloadRegistrar.of(event);

		for (var s : AutoPacket.SCANNED.get()) {
			if (s.to().contains(AutoPacket.To.CLIENT) && s.to().contains(AutoPacket.To.SERVER)) {
				reg.bidi(s.type());
			} else if (s.to().contains(AutoPacket.To.CLIENT)) {
				reg.s2c(s.type());
			} else if (s.to().contains(AutoPacket.To.SERVER)) {
				reg.c2s(s.type());
			}
		}
	}

	@SubscribeEvent
	public static void buildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.OP_BLOCKS) {
			for (var entry : ToolItem.REGISTRY.entrySet()) {
				var stack = entry.getValue().createItem();
				stack.set(DataComponents.ITEM_NAME, entry.getValue().getName());
				var tag = new CompoundTag();
				tag.putString("shimmer:tool", entry.getKey());
				stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
				stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
				event.accept(stack);
			}
		}
	}
}
