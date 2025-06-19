package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.opengl.GlCommandEncoder;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(GlCommandEncoder.class)
public class GlCommandEncoderMixin {
	@ModifyConstant(method = "presentTexture", constant = {
		@Constant(intValue = 0, ordinal = 0),
		// @Constant(intValue = 0, ordinal = 8),
		@Constant(intValue = 0, ordinal = 12)
	})
	private int vl$x(int original) {
		return ImGuiHooks.frameX(original);
	}

	@ModifyConstant(method = "presentTexture", constant = {
		@Constant(intValue = 0, ordinal = 1),
		// @Constant(intValue = 0, ordinal = 9),
		@Constant(intValue = 0, ordinal = 13)
	})
	private int vl$y(int original) {
		return ImGuiHooks.frameY(original);
	}

	@ModifyExpressionValue(method = "presentTexture", at = {
		@At(value = "INVOKE", target = "Lcom/mojang/blaze3d/textures/GpuTexture;getWidth(I)I", ordinal = 0),
		// @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/textures/GpuTexture;getWidth(I)I", ordinal = 1),
		@At(value = "INVOKE", target = "Lcom/mojang/blaze3d/textures/GpuTexture;getWidth(I)I", ordinal = 2)
	})
	private int vl$width(int original) {
		return ImGuiHooks.frameW(original);
	}

	@ModifyExpressionValue(method = "presentTexture", at = {
		@At(value = "INVOKE", target = "Lcom/mojang/blaze3d/textures/GpuTexture;getHeight(I)I", ordinal = 0),
		// @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/textures/GpuTexture;getHeight(I)I", ordinal = 1),
		@At(value = "INVOKE", target = "Lcom/mojang/blaze3d/textures/GpuTexture;getHeight(I)I", ordinal = 2)
	})
	private int vl$height(int original) {
		return ImGuiHooks.frameH(original);
	}
}
