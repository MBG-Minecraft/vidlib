package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public abstract class InventoryMixin {
	@Shadow
	@Final
	public Player player;

	@Shadow
	@Final
	private NonNullList<ItemStack> items;

	@Shadow
	private int selected;

	@Inject(method = "tick", at = @At("RETURN"))
	private void vl$tick(CallbackInfo ci) {
		for (int i = 0; i < items.size(); i++) {
			var stack = items.get(i);

			if (!stack.isEmpty()) {
				var updated = CommonGameEngine.INSTANCE.itemInventoryTick(stack, player, i == this.selected ? EquipmentSlot.MAINHAND : null);

				if (stack != updated || updated.isEmpty()) {
					items.set(i, updated.isEmpty() ? ItemStack.EMPTY : updated);
				}
			}
		}
	}
}
