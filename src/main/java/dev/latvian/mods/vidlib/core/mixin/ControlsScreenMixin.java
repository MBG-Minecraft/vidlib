package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.options.controls.ControlsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ControlsScreen.class)
public class ControlsScreenMixin {
	@ModifyReturnValue(method = "options", at = @At("RETURN"))
	private static OptionInstance<?>[] vl$options(OptionInstance<?>[] original) {
		var insert = ClientGameEngine.INSTANCE.insertControlsOptions();
		var arr = new OptionInstance<?>[original.length + insert.length];
		System.arraycopy(original, 0, arr, 0, original.length);
		System.arraycopy(insert, 0, arr, original.length, insert.length);
		return arr;
	}
}
