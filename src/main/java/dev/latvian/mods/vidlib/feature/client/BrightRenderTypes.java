package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.vidlib.feature.canvas.CanvasRenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.TriState;

public interface BrightRenderTypes {
	TexturedRenderType POS = TexturedRenderType.internal(
		"fullbright/cull/pos",
		1536,
		true,
		true,
		CanvasRenderPipelines.POS,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.createCompositeState(false)
	);

	TexturedRenderType POS_NO_CULL = TexturedRenderType.internal(
		"fullbright/no_cull/pos",
		1536,
		true,
		true,
		CanvasRenderPipelines.POS_NO_CULL,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.createCompositeState(false)
	);

	TexturedRenderType POS_COL = TexturedRenderType.internal(
		"fullbright/cull/pos_col",
		1536,
		true,
		true,
		CanvasRenderPipelines.POS_COL,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.createCompositeState(false)
	);

	TexturedRenderType POS_COL_NO_CULL = TexturedRenderType.internal(
		"fullbright/no_cull/pos_col",
		1536,
		true,
		true,
		CanvasRenderPipelines.POS_COL_NO_CULL,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.createCompositeState(false)
	);

	TexturedRenderType POS_TEX_COL = TexturedRenderType.internal(
		"fullbright/cull/pos_tex_col",
		1536,
		true,
		true,
		CanvasRenderPipelines.POS_TEX_COL,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.createCompositeState(false)
	);

	TexturedRenderType POS_TEX_COL_NO_CULL = TexturedRenderType.internal(
		"fullbright/no_cull/pos_tex_col",
		1536,
		true,
		true,
		CanvasRenderPipelines.POS_TEX_COL_NO_CULL,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.createCompositeState(false)
	);
}
