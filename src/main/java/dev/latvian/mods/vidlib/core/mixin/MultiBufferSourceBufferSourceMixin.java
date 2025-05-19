package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.latvian.mods.vidlib.feature.canvas.CanvasRenderPipelines;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MultiBufferSource.BufferSource.class)
public class MultiBufferSourceBufferSourceMixin {
	@ModifyReturnValue(method = "getBuffer", at = @At("RETURN"))
	private VertexConsumer vl$getBuffer(VertexConsumer buffer, @Local(argsOnly = true) RenderType type) {
		return CanvasRenderPipelines.wrap(buffer, type.getRenderPipeline());
	}
}
