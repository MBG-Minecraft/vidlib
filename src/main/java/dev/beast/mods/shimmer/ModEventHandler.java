package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.core.ShimmerPayloadRegistrar;
import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.block.filter.BlockFilter;
import dev.beast.mods.shimmer.feature.camerashake.CameraShakeType;
import dev.beast.mods.shimmer.feature.cutscene.event.CutsceneEvent;
import dev.beast.mods.shimmer.feature.entity.filter.EntityFilter;
import dev.beast.mods.shimmer.feature.misc.InternalPlayerData;
import dev.beast.mods.shimmer.feature.toolitem.PositionToolItem;
import dev.beast.mods.shimmer.feature.toolitem.ToolItem;
import dev.beast.mods.shimmer.feature.zone.shape.ZoneShape;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumber;
import dev.beast.mods.shimmer.math.worldposition.WorldPosition;
import dev.beast.mods.shimmer.util.KnownCodec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventHandler {
	@SubscribeEvent
	public static void afterLoad(FMLLoadCompleteEvent event) {
		KnownCodec.bootstrap();
		WorldNumber.bootstrap();
		WorldPosition.bootstrap();
		EntityFilter.bootstrap();
		BlockFilter.bootstrap();
		ZoneShape.bootstrap();
		CameraShakeType.bootstrap();
		InternalPlayerData.bootstrap();
		CutsceneEvent.bootstrap();

		ToolItem.REGISTRY.put("pos", new PositionToolItem());
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
