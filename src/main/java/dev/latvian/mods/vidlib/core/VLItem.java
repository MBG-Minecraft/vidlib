package dev.latvian.mods.vidlib.core;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public interface VLItem {
	static void partiallyMergeCustomData(ItemStack stack, CompoundTag tag) {
		var data = stack.get(DataComponents.CUSTOM_DATA);

		if (data == null || data.isEmpty()) {
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		} else {
			var newTag = data.copyTag();

			for (var key : tag.keySet()) {
				newTag.put(key, tag.get(key));
			}

			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(newTag));
		}
	}
}
