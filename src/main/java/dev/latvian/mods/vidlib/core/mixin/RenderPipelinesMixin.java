package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderPipelines;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderPipelines.class)
public class RenderPipelinesMixin {
	@ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderPipelines;register(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;)Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;"))
	private static RenderPipeline vl$builder(RenderPipeline original) {
		return VidLibRenderPipelines.wrap(original);
	}
}
