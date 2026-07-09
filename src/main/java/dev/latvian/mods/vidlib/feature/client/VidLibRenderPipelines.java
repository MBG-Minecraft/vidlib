package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.bloom.BloomRenderTypes;
import dev.latvian.mods.vidlib.feature.canvas.CanvasRenderPipelines;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticlesRenderTypes;
import dev.latvian.mods.vidlib.util.TerrainRenderLayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

@EventBusSubscriber(modid = ID.vidlib, value = Dist.CLIENT)
public interface VidLibRenderPipelines {
	RenderPipeline GUI_DEPTH = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
		.withLocation(ID.vidlib("pipeline/gui_depth"))
		.withFragmentShader(ID.vidlib("core/gui_depth"))
		.build();

	RenderPipeline MASKED_GUI = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
		.withLocation(ID.vidlib("pipeline/masked_gui"))
		.withFragmentShader(ID.vidlib("core/masked_gui"))
		.withSampler("Sampler1")
		.build();

	RenderPipeline MSDF = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
		.withLocation(ID.vidlib("pipeline/msdf"))
		.withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
		.withVertexShader(ID.vidlib("core/msdf"))
		.withFragmentShader(ID.vidlib("core/msdf"))
		.withSampler("Sampler0")
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.withCull(true)
		.build();

	RenderPipeline MSDF_SEE_THROUGH = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
		.withLocation(ID.vidlib("pipeline/msdf_see_through"))
		.withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
		.withVertexShader(ID.vidlib("core/msdf"))
		.withFragmentShader(ID.vidlib("core/msdf"))
		.withSampler("Sampler0")
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.withCull(true)
		.withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
		.build();

	RenderPipeline SKYBOX = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
		.withLocation(ID.vidlib("pipeline/skybox"))
		.withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
		.withVertexShader("core/position_tex_color")
		.withFragmentShader("core/position_tex_color")
		.withSampler("Sampler0")
		.withCull(true)
		.build();

	RenderPipeline SOLID_TERRAIN_NO_CULL = RenderPipeline.builder(RenderPipelines.TERRAIN_SNIPPET)
		.withLocation(ID.vidlib("pipeline/terrain/solid_no_cull"))
		.withCull(false)
		.build();

	RenderPipeline CUTOUT_MIPPED_TERRAIN_NO_CULL = RenderPipeline.builder(RenderPipelines.TERRAIN_SNIPPET)
		.withLocation(ID.vidlib("pipeline/terrain/cutout_mipped_no_cull"))
		.withShaderDefine("ALPHA_CUTOUT", 0.5F)
		.withCull(false)
		.build();

	RenderPipeline CUTOUT_TERRAIN_NO_CULL = RenderPipeline.builder(RenderPipelines.TERRAIN_SNIPPET)
		.withLocation(ID.vidlib("pipeline/terrain/cutout_no_cull"))
		.withShaderDefine("ALPHA_CUTOUT", 0.1F)
		.withCull(false)
		.build();

	RenderPipeline TRANSLUCENT_TERRAIN_NO_CULL = RenderPipeline.builder(RenderPipelines.TERRAIN_SNIPPET)
		.withLocation(ID.vidlib("pipeline/terrain/translucent_no_cull"))
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.withCull(false)
		.build();

	RenderPipeline ADDITIVE_PARTICLE = RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
		.withLocation(ID.vidlib("pipeline/particle/additive"))
		.withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
		.withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
		.build();

	RenderPipeline ADDITIVE_PARTICLE_ONLY_DEPTH = RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
		.withLocation(ID.vidlib("pipeline/particle/additive_only_depth"))
		.withColorTargetState(new ColorTargetState(Optional.of(BlendFunction.ADDITIVE), ColorTargetState.WRITE_NONE))
		.build();

	Function<BlendFunction, RenderPipeline> CANVAS_PIPELINES = Util.memoize(blendFunction -> RenderPipeline.builder()
		.withLocation(ID.vidlib("pipeline/canvas/" + blendFunction.sourceColor().name().toLowerCase(Locale.ROOT) + "/" + blendFunction.destColor().name().toLowerCase(Locale.ROOT) + "/" + blendFunction.sourceAlpha().name().toLowerCase(Locale.ROOT) + "/" + blendFunction.destAlpha().name().toLowerCase(Locale.ROOT) + "/"))
		.withVertexShader("core/screenquad")
		.withFragmentShader("core/blit_screen")
		.withSampler("InSampler")
		.withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
		.withColorTargetState(new ColorTargetState(Optional.of(blendFunction), ColorTargetState.WRITE_COLOR))
		.withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES)
		.build()
	);

	RenderPipeline.Snippet OUTLINE_SNIPPET = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
		.withVertexShader("core/rendertype_outline")
		.withFragmentShader("core/rendertype_outline")
		.withSampler("Sampler0")
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
		.buildSnippet();

	RenderPipeline OUTLINE_CULL = RenderPipeline.builder(OUTLINE_SNIPPET).withLocation("pipeline/outline_cull").build();
	RenderPipeline OUTLINE_NO_CULL = RenderPipeline.builder(OUTLINE_SNIPPET).withLocation("pipeline/outline_no_cull").withCull(false).build();

	RenderPipeline STONE_ENTITY_NO_CULL = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
		.withLocation(ID.vidlib("pipeline/canvas/no_cull/stone_entity"))
		.withFragmentShader(ID.vidlib("core/stone_entity"))
		.withSampler("Sampler1")
		.withCull(false)
		.withVertexFormat(DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS)
		.build();

	@SubscribeEvent
	static void registerRenderPipelines(RegisterRenderPipelinesEvent event) {
		event.registerPipeline(GUI_DEPTH);
		event.registerPipeline(MASKED_GUI);
		event.registerPipeline(MSDF);
		event.registerPipeline(SKYBOX);
		event.registerPipeline(SOLID_TERRAIN_NO_CULL);
		event.registerPipeline(CUTOUT_MIPPED_TERRAIN_NO_CULL);
		event.registerPipeline(CUTOUT_TERRAIN_NO_CULL);
		event.registerPipeline(TRANSLUCENT_TERRAIN_NO_CULL);
		event.registerPipeline(ADDITIVE_PARTICLE);
		event.registerPipeline(ADDITIVE_PARTICLE_ONLY_DEPTH);
		event.registerPipeline(PhysicsParticlesRenderTypes.SOLID_PIPELINE);
		event.registerPipeline(PhysicsParticlesRenderTypes.CUTOUT_PIPELINE);
		event.registerPipeline(PhysicsParticlesRenderTypes.TRANSLUCENT_PIPELINE);
		event.registerPipeline(STONE_ENTITY_NO_CULL);
		CanvasRenderPipelines.register(event);

		TerrainRenderLayer.SOLID.setClientValues(null, ChunkSectionLayer.SOLID, TerrainRenderTypes.SOLID, TerrainRenderTypes.SOLID_NO_CULL);
		TerrainRenderLayer.CUTOUT_MIPPED.setClientValues(null, ChunkSectionLayer.CUTOUT, TerrainRenderTypes.CUTOUT_MIPPED, TerrainRenderTypes.CUTOUT_MIPPED_NO_CULL);
		TerrainRenderLayer.CUTOUT.setClientValues(null, ChunkSectionLayer.CUTOUT, TerrainRenderTypes.CUTOUT, TerrainRenderTypes.CUTOUT_NO_CULL);
		TerrainRenderLayer.TRANSLUCENT.setClientValues(null, ChunkSectionLayer.TRANSLUCENT, TerrainRenderTypes.TRANSLUCENT, TerrainRenderTypes.TRANSLUCENT_NO_CULL);
		TerrainRenderLayer.TRIPWIRE.setClientValues(null, ChunkSectionLayer.TRANSLUCENT, TerrainRenderTypes.TRANSLUCENT, TerrainRenderTypes.TRANSLUCENT_NO_CULL);
		TerrainRenderLayer.PARTICLE.setClientValues(null, ChunkSectionLayer.TRANSLUCENT, TerrainRenderTypes.TRANSLUCENT, TerrainRenderTypes.TRANSLUCENT_NO_CULL);
		TerrainRenderLayer.BRIGHT.setClientValues(null, ChunkSectionLayer.TRANSLUCENT, BrightRenderTypes.POS_TEX_COL, BrightRenderTypes.POS_TEX_COL_NO_CULL);
		TerrainRenderLayer.BLOOM.setClientValues(null, ChunkSectionLayer.TRANSLUCENT, BloomRenderTypes.POS_TEX_COL, BloomRenderTypes.POS_TEX_COL_NO_CULL);
	}

	static RenderPipeline wrap(RenderPipeline original) {
		if (OUTLINE_CULL.getLocation().equals(original.getLocation())) {
			return OUTLINE_CULL;
		} else if (OUTLINE_NO_CULL.getLocation().equals(original.getLocation())) {
			return OUTLINE_NO_CULL;
		} else {
			return original;
		}
	}
}
