package dev.latvian.mods.vidlib.feature.bloom;

import dev.latvian.mods.vidlib.feature.canvas.CanvasRenderPipelines;
import dev.latvian.mods.vidlib.feature.client.TexturedRenderType;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.TriState;

public interface BloomRenderTypes {
	TexturedRenderType POS = TexturedRenderType.internal(
		"bloom/cull/pos",
		1536,
		true,
		true,
		CanvasRenderPipelines.POS,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.setOutputState(Bloom.CANVAS.getOutputStateShard())
			.createCompositeState(false)
	);

	TexturedRenderType POS_NO_CULL = TexturedRenderType.internal(
		"bloom/no_cull/pos",
		1536,
		true,
		true,
		CanvasRenderPipelines.POS_NO_CULL,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.setOutputState(Bloom.CANVAS.getOutputStateShard())
			.createCompositeState(false)
	);

	TexturedRenderType POS_COL = TexturedRenderType.internal(
		"bloom/cull/pos_col",
		1536,
		true,
		true,
		CanvasRenderPipelines.POS_COL,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.setOutputState(Bloom.CANVAS.getOutputStateShard())
			.createCompositeState(false)
	);

	TexturedRenderType POS_COL_NO_CULL = TexturedRenderType.internal(
		"bloom/no_cull/pos_col",
		1536,
		true,
		true,
		CanvasRenderPipelines.POS_COL_NO_CULL,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.setOutputState(Bloom.CANVAS.getOutputStateShard())
			.createCompositeState(false)
	);

	TexturedRenderType POS_TEX_COL = TexturedRenderType.internal(
		"bloom/cull/pos_tex_col",
		1536,
		true,
		true,
		CanvasRenderPipelines.POS_TEX_COL,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.setOutputState(Bloom.CANVAS.getOutputStateShard())
			.createCompositeState(false)
	);

	TexturedRenderType POS_TEX_COL_NO_CULL = TexturedRenderType.internal(
		"bloom/no_cull/pos_tex_col",
		1536,
		true,
		true,
		CanvasRenderPipelines.POS_TEX_COL_NO_CULL,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.setOutputState(Bloom.CANVAS.getOutputStateShard())
			.createCompositeState(false)
	);
}
