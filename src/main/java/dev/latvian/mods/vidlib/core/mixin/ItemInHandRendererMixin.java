package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.util.MiscUtils;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
	@Redirect(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;isInvisible()Z"))
	private boolean vl$isInvisible(final AbstractClientPlayer player) {
		return player.isSpectator();
	}

	@Inject(method = "shouldInstantlyReplaceVisibleItem", at = @At("HEAD"), cancellable = true)
	private void vl$shouldInstantlyReplaceVisibleItem(ItemStack oldItem, ItemStack newItem, CallbackInfoReturnable<Boolean> cir) {
		if (oldItem.getCount() == newItem.getCount() && oldItem.getItem() == newItem.getItem() && MiscUtils.NO_BOB_ITEMS.contains(newItem.getItem())) {
			cir.setReturnValue(true);
		}
	}
}
