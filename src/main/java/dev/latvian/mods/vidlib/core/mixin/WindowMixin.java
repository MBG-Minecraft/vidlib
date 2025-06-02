package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.blaze3d.platform.Window;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Window.class)
public class WindowMixin {
	@Shadow
	private int framebufferWidth;

	@Shadow
	private int framebufferHeight;

	@Inject(method = "calculateScale", at = @At("HEAD"), cancellable = true)
	private void vl$calculateScale(int guiScale, boolean forceUnicode, CallbackInfoReturnable<Integer> cir) {
		int s = MiscClientUtils.calculateScale(framebufferWidth, framebufferHeight);

		if (s != -1) {
			cir.setReturnValue(s);
		}
	}
}
