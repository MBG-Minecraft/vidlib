package dev.latvian.mods.vidlib.feature.canvas;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.latvian.mods.klib.render.vertexconsumer.PosColTexVertexConsumer;
import dev.latvian.mods.klib.render.vertexconsumer.PosColVertexConsumer;
import dev.latvian.mods.klib.render.vertexconsumer.PosVertexConsumer;
import dev.latvian.mods.klib.util.ID;
import net.minecraft.client.renderer.RenderPipelines;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

public interface CanvasRenderPipelines {
	RenderPipeline.Snippet SNIPPET = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
		.withVertexShader("core/position_tex_color")
		.withFragmentShader("core/position_tex_color")
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.withCull(true)
		.buildSnippet();

	RenderPipeline.Snippet SNIPPET_NO_CULL = RenderPipeline.builder(SNIPPET)
		.withCull(false)
		.buildSnippet();

	RenderPipeline.Snippet TEXTURE_SNIPPET = RenderPipeline.builder()
		.withSampler("Sampler0")
		.buildSnippet();

	RenderPipeline POS = RenderPipeline.builder(SNIPPET, RenderPipelines.FOG_SNIPPET)
		.withLocation(ID.vidlib("pipeline/canvas/cull/pos"))
		.withVertexShader("core/position")
		.withFragmentShader("core/position")
		.withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
		.build();

	RenderPipeline POS_NO_CULL = RenderPipeline.builder(SNIPPET_NO_CULL, RenderPipelines.FOG_SNIPPET)
		.withLocation(ID.vidlib("pipeline/canvas/no_cull/pos"))
		.withVertexShader("core/position")
		.withFragmentShader("core/position")
		.withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
		.build();

	RenderPipeline POS_COL = RenderPipeline.builder(SNIPPET, RenderPipelines.FOG_SNIPPET)
		.withLocation(ID.vidlib("pipeline/canvas/cull/pos_col"))
		.withVertexShader("core/position_color")
		.withFragmentShader("core/position_color")
		.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
		.build();

	RenderPipeline POS_COL_NO_CULL = RenderPipeline.builder(SNIPPET_NO_CULL, RenderPipelines.FOG_SNIPPET)
		.withLocation(ID.vidlib("pipeline/canvas/no_cull/pos_col"))
		.withVertexShader("core/position_color")
		.withFragmentShader("core/position_color")
		.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
		.build();

	RenderPipeline POS_TEX_COL = RenderPipeline.builder(SNIPPET, TEXTURE_SNIPPET)
		.withLocation(ID.vidlib("pipeline/canvas/cull/pos_tex_col"))
		.withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
		.build();

	RenderPipeline POS_TEX_COL_NO_CULL = RenderPipeline.builder(SNIPPET_NO_CULL, TEXTURE_SNIPPET)
		.withLocation(ID.vidlib("pipeline/canvas/no_cull/pos_tex_col"))
		.withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
		.build();

	RenderPipeline ENTITY = RenderPipeline.builder(SNIPPET, TEXTURE_SNIPPET)
		.withLocation(ID.vidlib("pipeline/canvas/cull/entity"))
		.withVertexShader(ID.vidlib("core/bright_entity"))
		.withFragmentShader(ID.vidlib("core/bright"))
		.withVertexFormat(DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS)
		.build();

	RenderPipeline ENTITY_NO_CULL = RenderPipeline.builder(SNIPPET_NO_CULL, TEXTURE_SNIPPET)
		.withLocation(ID.vidlib("pipeline/canvas/no_cull/entity"))
		.withVertexShader(ID.vidlib("core/bright_entity"))
		.withFragmentShader(ID.vidlib("core/bright"))
		.withVertexFormat(DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS)
		.build();

	RenderPipeline BLOCK = RenderPipeline.builder(SNIPPET, TEXTURE_SNIPPET)
		.withLocation(ID.vidlib("pipeline/canvas/cull/block"))
		.withVertexShader(ID.vidlib("core/bright_block"))
		.withFragmentShader(ID.vidlib("core/bright"))
		.withVertexFormat(DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS)
		.build();

	RenderPipeline BLOCK_NO_CULL = RenderPipeline.builder(SNIPPET_NO_CULL, TEXTURE_SNIPPET)
		.withLocation(ID.vidlib("pipeline/canvas/no_cull/block"))
		.withVertexShader(ID.vidlib("core/bright_block"))
		.withFragmentShader(ID.vidlib("core/bright"))
		.withVertexFormat(DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS)
		.build();

	static void register(RegisterRenderPipelinesEvent event) {
		event.registerPipeline(POS);
		event.registerPipeline(POS_NO_CULL);
		event.registerPipeline(POS_COL);
		event.registerPipeline(POS_COL_NO_CULL);
		event.registerPipeline(POS_TEX_COL);
		event.registerPipeline(POS_TEX_COL_NO_CULL);
		event.registerPipeline(ENTITY);
		event.registerPipeline(ENTITY_NO_CULL);
		event.registerPipeline(BLOCK);
		event.registerPipeline(BLOCK_NO_CULL);
	}

	static VertexConsumer wrap(VertexConsumer buffer, RenderPipeline pipeline) {
		if (pipeline == POS || pipeline == POS_NO_CULL) {
			return new PosVertexConsumer(buffer);
		} else if (pipeline == POS_COL || pipeline == POS_COL_NO_CULL) {
			return new PosColVertexConsumer(buffer);
		} else if (pipeline == POS_TEX_COL || pipeline == POS_TEX_COL_NO_CULL) {
			return new PosColTexVertexConsumer(buffer);
		} else {
			return buffer;
		}
	}
}
