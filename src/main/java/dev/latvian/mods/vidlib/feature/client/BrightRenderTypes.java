package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.vidlib.feature.canvas.CanvasRenderPipelines;
import dev.latvian.mods.vidlib.feature.texture.SpriteKey;
import dev.latvian.mods.vidlib.util.client.MultiBufferSourceOverride;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.TriState;

public interface BrightRenderTypes {
	TexturedRenderType POS = TexturedRenderType.internal(
		"bright/cull/pos",
		1536,
		true,
		true,
		CanvasRenderPipelines.POS,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.createCompositeState(false)
	);

	TexturedRenderType POS_NO_CULL = TexturedRenderType.internal(
		"bright/no_cull/pos",
		1536,
		true,
		true,
		CanvasRenderPipelines.POS_NO_CULL,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.createCompositeState(false)
	);

	TexturedRenderType POS_COL = TexturedRenderType.internal(
		"bright/cull/pos_col",
		1536,
		true,
		true,
		CanvasRenderPipelines.POS_COL,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.createCompositeState(false)
	);

	TexturedRenderType POS_COL_NO_CULL = TexturedRenderType.internal(
		"bright/no_cull/pos_col",
		1536,
		true,
		true,
		CanvasRenderPipelines.POS_COL_NO_CULL,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.createCompositeState(false)
	);

	TexturedRenderType POS_TEX_COL = TexturedRenderType.internal(
		"bright/cull/pos_tex_col",
		1536,
		true,
		true,
		CanvasRenderPipelines.POS_TEX_COL,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.createCompositeState(false)
	);

	TexturedRenderType POS_TEX_COL_NO_CULL = TexturedRenderType.internal(
		"bright/no_cull/pos_tex_col",
		1536,
		true,
		true,
		CanvasRenderPipelines.POS_TEX_COL_NO_CULL,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.createCompositeState(false)
	);

	TexturedRenderType ENTITY = TexturedRenderType.internal(
		"bright/cull/entity",
		1536,
		true,
		true,
		CanvasRenderPipelines.ENTITY,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.createCompositeState(false)
	);

	TexturedRenderType ENTITY_NO_CULL = TexturedRenderType.internal(
		"bright/no_cull/entity",
		1536,
		true,
		true,
		CanvasRenderPipelines.ENTITY_NO_CULL,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.createCompositeState(false)
	);

	TexturedRenderType BLOCK = TexturedRenderType.internal(
		"bright/cull/block",
		1536,
		true,
		true,
		CanvasRenderPipelines.BLOCK,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.createCompositeState(false)
	);

	TexturedRenderType BLOCK_NO_CULL = TexturedRenderType.internal(
		"bright/no_cull/block",
		1536,
		true,
		true,
		CanvasRenderPipelines.BLOCK_NO_CULL,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.createCompositeState(false)
	);

	RenderType DEFAULT_BLOCK = BLOCK.apply(SpriteKey.BLOCKS);
	RenderType DEFAULT_BLOCK_NO_CULL = BLOCK_NO_CULL.apply(SpriteKey.BLOCKS);

	static MultiBufferSourceOverride overridePos(MultiBufferSource delegate) {
		return new MultiBufferSourceOverride(delegate, POS, POS_NO_CULL);
	}

	static MultiBufferSourceOverride overridePosCol(MultiBufferSource delegate) {
		return new MultiBufferSourceOverride(delegate, POS_COL, POS_COL_NO_CULL);
	}

	static MultiBufferSourceOverride overridePosTexCol(MultiBufferSource delegate) {
		return new MultiBufferSourceOverride(delegate, POS_TEX_COL, POS_TEX_COL_NO_CULL);
	}

	static MultiBufferSourceOverride overrideEntityCutout(MultiBufferSource delegate) {
		return new MultiBufferSourceOverride(delegate, ENTITY, ENTITY_NO_CULL);
	}

	static MultiBufferSourceOverride overrideBlock(MultiBufferSource delegate) {
		return new MultiBufferSourceOverride(delegate, BLOCK, BLOCK_NO_CULL);
	}
}
