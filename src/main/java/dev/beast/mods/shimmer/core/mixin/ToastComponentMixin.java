package dev.beast.mods.shimmer.core.mixin;

import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastComponent.class)
public class ToastComponentMixin {
	@Inject(method = "addToast", at = @At("HEAD"), cancellable = true)
	private void shimmer$add(Toast toast, CallbackInfo ci) {
		if (toast instanceof TutorialToast || toast instanceof AdvancementToast || toast instanceof RecipeToast) {
			ci.cancel();
		}
	}
}
