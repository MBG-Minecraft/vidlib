package dev.latvian.mods.vidlib.feature.gallery;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.klib.util.client.FramebufferUtils;

import java.util.function.Consumer;
import java.util.function.Function;

public class OffscreenRenderer {
	private static final int BUFFER_SIZE = 65536;

	public static <T> T render(
		int width,
		int height,
		boolean useDepth,
		Consumer<PoseStack> poseSetup,
		Consumer<OffscreenRenderer> body,
		Function<RenderTarget, T> capture
	) {
		var target = new TextureTarget("VidLibGalleryCanvas", width, height, com.mojang.renderpearl.api.GpuFormat.RGBA8_UNORM, useDepth ? com.mojang.renderpearl.api.GpuFormat.D32_FLOAT : null);

		try {
			var renderer = new OffscreenRenderer();
			var poseStack = new PoseStack();
			poseSetup.accept(poseStack);
			body.accept(renderer);
			// renderer.draw(target);
			return capture.apply(target);
		} finally {
			target.destroyBuffers();
		}
	}

	public static NativeImage image(int width, int height, boolean useDepth, Consumer<PoseStack> poseSetup, Consumer<OffscreenRenderer> body) {
		return render(width, height, useDepth, poseSetup, body, FramebufferUtils::capture);
	}
}
