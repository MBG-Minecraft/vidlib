package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.imgui.ImGuiHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "com.mojang.blaze3d.opengl.GlCommandEncoder")
public class GlCommandEncoderMixin {
	@ModifyArg(method = "presentTexture", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_viewport(IIII)V"), index = 0)
	private int vl$viewportX(int original) {
		return ImGuiHooks.frameX(original);
	}

	@ModifyArg(method = "presentTexture", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_viewport(IIII)V"), index = 1)
	private int vl$viewportY(int original) {
		return ImGuiHooks.frameY(original);
	}

	@ModifyArg(method = "presentTexture", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_viewport(IIII)V"), index = 2)
	private int vl$viewportWidth(int original) {
		return ImGuiHooks.frameW(original);
	}

	@ModifyArg(method = "presentTexture", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_viewport(IIII)V"), index = 3)
	private int vl$viewportHeight(int original) {
		return ImGuiHooks.frameH(original);
	}

	@ModifyArg(method = "presentTexture", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/DirectStateAccess;blitFrameBuffers(IIIIIIIIIIII)V"), index = 6)
	private int vl$blitDestX(int original) {
		return ImGuiHooks.frameX(original);
	}

	@ModifyArg(method = "presentTexture", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/DirectStateAccess;blitFrameBuffers(IIIIIIIIIIII)V"), index = 7)
	private int vl$blitDestY(int original) {
		return ImGuiHooks.frameY(original);
	}

	@ModifyArg(method = "presentTexture", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/DirectStateAccess;blitFrameBuffers(IIIIIIIIIIII)V"), index = 8)
	private int vl$blitDestWidth(int original) {
		return ImGuiHooks.frameW(original);
	}

	@ModifyArg(method = "presentTexture", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/DirectStateAccess;blitFrameBuffers(IIIIIIIIIIII)V"), index = 9)
	private int vl$blitDestHeight(int original) {
		return ImGuiHooks.frameH(original);
	}
}
