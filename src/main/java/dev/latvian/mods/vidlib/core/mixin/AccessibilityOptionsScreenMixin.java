package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.latvian.mods.vidlib.feature.client.VidLibClientOptions;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AccessibilityOptionsScreen.class)
public class AccessibilityOptionsScreenMixin {
	@ModifyReturnValue(method = "options", at = @At("RETURN"))
	private static OptionInstance<?>[] vl$options(OptionInstance<?>[] original) {
		var arr = new OptionInstance<?>[original.length + VidLibClientOptions.ACCESSIBILITY_OPTIONS.length];
		System.arraycopy(original, 0, arr, 0, original.length);
		System.arraycopy(VidLibClientOptions.ACCESSIBILITY_OPTIONS, 0, arr, original.length, VidLibClientOptions.ACCESSIBILITY_OPTIONS.length);
		return arr;
	}
}
