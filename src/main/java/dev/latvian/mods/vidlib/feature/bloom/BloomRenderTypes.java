package dev.latvian.mods.vidlib.feature.bloom;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.latvian.mods.klib.render.BufferSupplier;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.klib.vertex.VertexCallback;
import dev.latvian.mods.vidlib.feature.canvas.CanvasRenderPipelines;
import dev.latvian.mods.vidlib.feature.client.TexturedRenderType;
import dev.latvian.mods.vidlib.feature.visual.SpriteKey;
import dev.latvian.mods.vidlib.util.client.MultiBufferSourceOverride;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;

public interface BloomRenderTypes {
	TexturedRenderType POS = bloom("bloom/cull/pos", CanvasRenderPipelines.POS);
	TexturedRenderType POS_NO_CULL = bloom("bloom/no_cull/pos", CanvasRenderPipelines.POS_NO_CULL);
	TexturedRenderType POS_COL = bloom("bloom/cull/pos_col", CanvasRenderPipelines.POS_COL);
	TexturedRenderType POS_COL_NO_CULL = bloom("bloom/no_cull/pos_col", CanvasRenderPipelines.POS_COL_NO_CULL);
	TexturedRenderType POS_TEX_COL = bloom("bloom/cull/pos_tex_col", CanvasRenderPipelines.POS_TEX_COL);
	TexturedRenderType POS_TEX_COL_NO_CULL = bloom("bloom/no_cull/pos_tex_col", CanvasRenderPipelines.POS_TEX_COL_NO_CULL);
	TexturedRenderType ENTITY_CUTOUT = bloom("bloom/cull/entity/cutout", CanvasRenderPipelines.ENTITY);
	TexturedRenderType ENTITY_CUTOUT_NO_CULL = bloom("bloom/no_cull/entity/cutout", CanvasRenderPipelines.ENTITY_NO_CULL);
	TexturedRenderType BLOCK = bloom("bloom/cull/block", CanvasRenderPipelines.BLOCK);
	TexturedRenderType BLOCK_NO_CULL = bloom("bloom/no_cull/block", CanvasRenderPipelines.BLOCK_NO_CULL);

	RenderType DEFAULT_BLOCK = BLOCK.apply(SpriteKey.BLOCKS.texturePath());
	RenderType DEFAULT_BLOCK_NO_CULL = BLOCK_NO_CULL.apply(SpriteKey.BLOCKS.texturePath());

	RenderType DEFAULT_POS_COL = POS_COL.apply(Empty.TEXTURE);
	RenderType DEFAULT_POS_COL_NO_CULL = POS_COL_NO_CULL.apply(Empty.TEXTURE);

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

	BufferSupplier POS_COL_BUFFER_SUPPLIER = BufferSupplier.fixed(DEFAULT_POS_COL, DEFAULT_POS_COL_NO_CULL).process(VertexCallback::onlyPosCol);

	BufferSupplier BLOCK_BUFFER_SUPPLIER = BufferSupplier.fixed(DEFAULT_BLOCK, DEFAULT_BLOCK_NO_CULL).process(VertexCallback::onlyPosColTex);

	private static TexturedRenderType bloom(String name, RenderPipeline pipeline) {
		return TexturedRenderType.internal(
			name,
			1536,
			true,
			true,
			texture -> TexturedRenderType.textured(pipeline, texture).setOutputTarget(Bloom.CANVAS.getOutputTargetBinding())
		);
	}
}
