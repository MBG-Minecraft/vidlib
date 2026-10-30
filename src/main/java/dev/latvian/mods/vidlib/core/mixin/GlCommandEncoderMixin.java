package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.backend.opengl.DirectStateAccess;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The game renders into a render target shrunk to the viewport area between ImGui's top and bottom
 * bars ({@code WindowMixin#vl$setViewportArea}). 26.3 composites that image onto the window through
 * {@code GlCommandEncoder.presentTexture}, which blits it bottom-aligned into the default
 * framebuffer. That has to be overridden so the image lands exactly in the viewport area instead:
 * the source rect is kept as-is and the destination rect is replaced with the viewport offset and
 * the source size.
 */
@Mixin(targets = "com.mojang.renderpearl.backend.opengl.GlCommandEncoder")
public class GlCommandEncoderMixin {
	@WrapOperation(method = "presentTexture", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/opengl/DirectStateAccess;blitFrameBuffers(IIIIIIIIIIII)V"))
	private void vl$blit(DirectStateAccess directStateAccess, int srcFbo, int dstFbo, int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter, Operation<Void> operation) {
		var window = net.minecraft.client.Minecraft.getInstance().getWindow();
		int x = ImGuiHooks.frameX(0);
		int y = ImGuiHooks.frameY(0);
		operation.call(directStateAccess, srcFbo, dstFbo, srcX0, srcY0, srcX1, srcY1, x, y, x + (srcX1 - srcX0), y + (srcY1 - srcY0), mask, filter);
	}
}
