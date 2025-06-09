package dev.latvian.mods.vidlib.feature.bloom;

import dev.latvian.mods.klib.render.BufferSupplier;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.klib.vertex.VertexCallback;
import dev.latvian.mods.vidlib.feature.canvas.CanvasRenderPipelines;
import dev.latvian.mods.vidlib.feature.client.TexturedRenderType;
import dev.latvian.mods.vidlib.feature.visual.SpriteKey;
import dev.latvian.mods.vidlib.util.client.MultiBufferSourceOverride;
import net.minecraft.client.renderer.MultiBufferSource;
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

	TexturedRenderType ENTITY_CUTOUT = TexturedRenderType.internal(
		"bloom/cull/entity/cutout",
		1536,
		true,
		true,
		CanvasRenderPipelines.ENTITY,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.setOutputState(Bloom.CANVAS.getOutputStateShard())
			.createCompositeState(false)
	);

	TexturedRenderType ENTITY_CUTOUT_NO_CULL = TexturedRenderType.internal(
		"bloom/no_cull/entity/cutout",
		1536,
		true,
		true,
		CanvasRenderPipelines.ENTITY_NO_CULL,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.setOutputState(Bloom.CANVAS.getOutputStateShard())
			.createCompositeState(false)
	);

	TexturedRenderType BLOCK = TexturedRenderType.internal(
		"bloom/cull/block",
		1536,
		true,
		true,
		CanvasRenderPipelines.BLOCK,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.setOutputState(Bloom.CANVAS.getOutputStateShard())
			.createCompositeState(false)
	);

	TexturedRenderType BLOCK_NO_CULL = TexturedRenderType.internal(
		"bloom/no_cull/block",
		1536,
		true,
		true,
		CanvasRenderPipelines.BLOCK_NO_CULL,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.setOutputState(Bloom.CANVAS.getOutputStateShard())
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
		return new MultiBufferSourceOverride(delegate, ENTITY_CUTOUT, ENTITY_CUTOUT_NO_CULL);
	}

	BufferSupplier POS_COL_BUFFER_SUPPLIER = BufferSupplier.fixed(POS_COL.apply(Empty.TEXTURE), POS_COL_NO_CULL.apply(Empty.TEXTURE)).process(VertexCallback::onlyPosCol);

	BufferSupplier BLOCK_BUFFER_SUPPLIER = BufferSupplier.fixed(DEFAULT_BLOCK, DEFAULT_BLOCK_NO_CULL).process(VertexCallback::onlyPosColTex);
}
