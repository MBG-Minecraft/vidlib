package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.systems.RenderPass;
import dev.latvian.mods.vidlib.core.VLWithCanvas;
import dev.latvian.mods.vidlib.feature.canvas.Canvas;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PostPass.class)
public class PostPassMixin implements VLWithCanvas {
	@Shadow
	@Final
	private String name;

	@Unique
	private Canvas vl$canvas;

	@Override
	public void vl$setCanvas(Canvas canvas) {
		vl$canvas = canvas;
	}

	@Redirect(method = "addToFrame", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FramePass;executes(Ljava/lang/Runnable;)V"))
	private void vl$executeIfCanvasActive(FramePass pass, Runnable runnable) {
		pass.executes(() -> {
			if (vl$canvas == null || vl$canvas.active) {
				runnable.run();
			}
		});
	}

	@Redirect(method = "lambda$addToFrame$1", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPass;draw(II)V"))
	private void vl$applyCanvasState(RenderPass pass, int firstVertex, int vertexCount) {
		if (vl$canvas != null) {
			vl$canvas.accept(pass);
		}

		pass.draw(firstVertex, vertexCount);
	}
}
