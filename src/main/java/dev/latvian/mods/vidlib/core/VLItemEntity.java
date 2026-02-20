package dev.latvian.mods.vidlib.core;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.crafting.Ingredient;

public interface VLItemEntity extends VLEntity {
	@Override
	default boolean vl$hasItem(Ingredient ingredient) {
		var item = ((ItemEntity) this).getItem();
		return !item.isEmpty() && ingredient.test(item);
	}
}
