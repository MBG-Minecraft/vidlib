package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
import com.mojang.blaze3d.resource.ResourceDescriptor;
import com.mojang.blaze3d.resource.ResourceHandle;
import dev.latvian.mods.vidlib.core.VLWithCanvas;
import dev.latvian.mods.vidlib.feature.canvas.Canvas;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(PostChain.class)
public class PostChainMixin implements VLWithCanvas {
	@Shadow
	@Final
	private List<PostPass> passes;

	@Override
	public void vl$setCanvas(Canvas canvas) {
		for (var pass : passes) {
			((VLWithCanvas) pass).vl$setCanvas(canvas);
		}
	}

	@Redirect(method = "addToFrame", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;createInternal(Ljava/lang/String;Lcom/mojang/blaze3d/resource/ResourceDescriptor;)Lcom/mojang/blaze3d/resource/ResourceHandle;"))
	private ResourceHandle<RenderTarget> vl$createRenderTarget(FrameGraphBuilder instance, String name, ResourceDescriptor<RenderTarget> descriptor, @Local(argsOnly = true, ordinal = 0) int fw, @Local(argsOnly = true, ordinal = 1) int fh) {
		if (descriptor instanceof RenderTargetDescriptor(int bw, int bh, boolean depth, int clearColor, boolean stencil) && (bw <= 0 || bh <= 0)) {
			int w = bw == 0 ? fw : bw < 0 ? (fw / -bw) : fw;
			int h = bh == 0 ? fh : bh < 0 ? (fh / -bh) : fh;
			descriptor = new RenderTargetDescriptor(w, h, depth, clearColor, stencil);
		}

		return instance.createInternal(name, descriptor);
	}
}
