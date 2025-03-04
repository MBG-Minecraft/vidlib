package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.feature.camerashake.CameraShakeType;
import dev.beast.mods.shimmer.feature.entity.EntityOverride;
import dev.beast.mods.shimmer.feature.entity.filter.EntityFilter;
import dev.beast.mods.shimmer.feature.zone.ZoneShape;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumber;
import dev.beast.mods.shimmer.math.worldposition.WorldPosition;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
		WorldNumber.bootstrap();
		WorldPosition.bootstrap();
		EntityFilter.bootstrap();
		ZoneShape.bootstrap();
		CameraShakeType.bootstrap();
		EntityOverride.PASS_THROUGH_BARRIERS.setGlobal(EntityType.PLAYER, Entity::shimmer$isCreative);
	}

	@SubscribeEvent
	static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
		var reg = event.registrar("1").optional();

		// reg.playToServer
	}

	public static ItemStack createTool(Item item, String name, String toolType) {
		var stack = new ItemStack(item);
		stack.set(DataComponents.ITEM_NAME, Component.literal(name));
		var posStickTag = new CompoundTag();
		posStickTag.putString("shimmer:tool", toolType);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(posStickTag));
		stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
		return stack;
	}

	@SubscribeEvent
	public static void buildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.OP_BLOCKS) {
			for (var item : BuiltInRegistries.ITEM) {
				if (item.builtInRegistryHolder().getKey().location().getNamespace().equals(Shimmer.ID)) {
					event.accept(item);
				}
			}

			event.accept(createTool(Items.BREEZE_ROD, "Position Tool", "pos"));
		}
	}
}
