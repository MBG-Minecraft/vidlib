package dev.latvian.mods.vidlib.feature.canvas;

import dev.latvian.mods.vidlib.feature.client.TexturedRenderType;
import dev.latvian.mods.vidlib.util.client.MultiBufferSourceOverride;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.TriState;

public interface BossRenderTypes {
	TexturedRenderType CULL = TexturedRenderType.internal(
		"boss/cull",
		1536,
		true,
		true,
		CanvasRenderPipelines.POS_TEX_COL,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.setOutputState(BossRendering.CANVAS.getOutputStateShard())
			.createCompositeState(false)
	);

	TexturedRenderType NO_CULL = TexturedRenderType.internal(
		"boss/no_cull",
		1536,
		true,
		true,
		CanvasRenderPipelines.POS_TEX_COL_NO_CULL,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.setOutputState(BossRendering.CANVAS.getOutputStateShard())
			.createCompositeState(false)
	);

	static MultiBufferSourceOverride override(MultiBufferSource delegate) {
		return new MultiBufferSourceOverride(delegate, CULL, NO_CULL);
	}
}