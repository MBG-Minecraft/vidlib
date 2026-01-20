package dev.latvian.mods.vidlib.feature.item;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record ItemKey(Holder<Item> item, DataComponentPatch patch) {
	@Override
	public boolean equals(Object obj) {
		return obj instanceof ItemKey key && item.is(key.item) && patch.equals(key.patch);
	}

	public ItemStack toItemStack() {
		return new ItemStack(item, 1, patch);
	}
}
