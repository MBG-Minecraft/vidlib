package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.RenderPass;
import dev.latvian.mods.vidlib.feature.client.GLDebugLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.Consumer;

@Mixin(PostPass.class)
public class PostPassMixin {
	@Shadow
	@Final
	private String name;

	@Redirect(method = "lambda$addToFrame$1", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPass;setUniform(Ljava/lang/String;[F)V"))
	private void vl$addToFrameResize(RenderPass instance, String name, float[] value) {
		if (name.equals("OutSize")) {
			var t = Minecraft.getInstance().getMainRenderTarget();
			instance.setUniform(name, (float) t.width, (float) t.height);
		} else {
			instance.setUniform(name, value);
		}
	}

	@Inject(method = "lambda$addToFrame$1", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPass;drawIndexed(II)V"))
	private void vl$addToFrameDraw(ResourceHandle<RenderTarget> resourcehandle, Matrix4f projectionMatrix, Map<ResourceLocation, ResourceHandle<RenderTarget>> targets, Consumer<RenderPass> uniformSetter, CallbackInfo ci) {
		GLDebugLog.message("Drawing post pass " + name);
	}
}
