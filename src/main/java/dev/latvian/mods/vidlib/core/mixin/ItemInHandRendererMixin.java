package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FirstPersonHandsAndItems.class)
public class ItemInHandRendererMixin {
	@Inject(method = "shouldInstantlyReplaceVisibleItem", at = @At("HEAD"), cancellable = true)
	private void vl$shouldInstantlyReplaceVisibleItem(ItemStack oldItem, ItemStack newItem, LocalPlayer player, CallbackInfoReturnable<Boolean> cir) {
		if (oldItem.getCount() == newItem.getCount() && oldItem.getItem() == newItem.getItem() && MiscClientUtils.NO_BOB_ITEMS.contains(newItem.getItem())) {
			cir.setReturnValue(true);
		}
	}
}
