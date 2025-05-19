package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import dev.latvian.mods.vidlib.feature.canvas.CanvasImpl;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelTargetBundle.class)
public class LevelTargetBundleMixin {
	@Inject(method = "get", at = @At("HEAD"), cancellable = true)
	private void vl$get(ResourceLocation id, CallbackInfoReturnable<ResourceHandle<RenderTarget>> cir) {
		var c = CanvasImpl.get(id);

		if (c != null) {
			cir.setReturnValue(c.getOutputTargetResource());
		}
	}

	@Inject(method = "replace", at = @At("HEAD"), cancellable = true)
	private void vl$replace(ResourceLocation id, ResourceHandle<RenderTarget> target, CallbackInfo ci) {
		if (CanvasImpl.replace(id, target)) {
			ci.cancel();
		}
	}

	@Inject(method = "clear", at = @At("RETURN"))
	private void vl$clear(CallbackInfo ci) {
		CanvasImpl.clearAll();
	}
}
