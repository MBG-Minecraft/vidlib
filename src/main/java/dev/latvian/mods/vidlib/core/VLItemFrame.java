package dev.latvian.mods.vidlib.core;

import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.crafting.Ingredient;

public interface VLItemFrame extends VLEntity {
	@Override
	default boolean vl$hasItem(Ingredient ingredient) {
		var item = ((ItemFrame) this).getItem();
		return !item.isEmpty() && ingredient.test(item);
	}
}
