package dev.latvian.mods.vidlib.feature.canvas;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.latvian.mods.vidlib.feature.client.TexturedRenderType;
import dev.latvian.mods.vidlib.util.client.MultiBufferSourceOverride;
import net.minecraft.client.renderer.MultiBufferSource;

public interface BossRenderTypes {
	TexturedRenderType CULL = boss("boss/cull", CanvasRenderPipelines.POS_TEX_COL);
	TexturedRenderType NO_CULL = boss("boss/no_cull", CanvasRenderPipelines.POS_TEX_COL_NO_CULL);

	static MultiBufferSourceOverride override(MultiBufferSource delegate) {
		return new MultiBufferSourceOverride(delegate, CULL, NO_CULL);
	}

	private static TexturedRenderType boss(String name, RenderPipeline pipeline) {
		return TexturedRenderType.internal(
			name,
			1536,
			true,
			true,
			texture -> TexturedRenderType.textured(pipeline, texture).setOutputTarget(BossRendering.CANVAS.getOutputTargetBinding())
		);
	}
}
