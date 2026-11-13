package dev.latvian.mods.vidlib.core.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.RenderPass;
import dev.latvian.mods.vidlib.feature.client.CustomPostPassUniforms;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PostPass.class)
public class PostPassMixin {
	@WrapOperation(method = "lambda$addToFrame$1", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/RenderPass;setUniform(Ljava/lang/String;Lcom/mojang/renderpearl/api/buffers/GpuBuffer;)V", ordinal = 1))
	private void vl$setUniform(RenderPass instance, String key, GpuBuffer buffer, Operation<Void> original) {
		if (!CustomPostPassUniforms.replace(instance, key)) {
			original.call(instance, key, buffer);
		}
	}
}
