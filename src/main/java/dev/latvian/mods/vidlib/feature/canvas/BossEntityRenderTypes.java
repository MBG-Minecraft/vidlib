package dev.latvian.mods.vidlib.feature.canvas;

import dev.latvian.mods.vidlib.feature.client.TexturedRenderType;
import dev.latvian.mods.vidlib.util.Empty;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.TriState;

public interface BossEntityRenderTypes {
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

	RenderType WHITE_CULL = CULL.apply(Empty.TEXTURE);
	RenderType WHITE_NO_CULL = NO_CULL.apply(Empty.TEXTURE);
}