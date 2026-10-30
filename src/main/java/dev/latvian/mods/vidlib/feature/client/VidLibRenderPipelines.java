package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.BlendEquation;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticlesRenderTypes;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

import java.util.Locale;
import java.util.function.Function;

@EventBusSubscriber(modid = VidLib.ID, value = Dist.CLIENT)
public interface VidLibRenderPipelines {
	private static String blendName(BlendFunction b) {
		BlendEquation c = b.color();
		BlendEquation a = b.alpha();
		return (c.sourceFactor().name() + "/" + c.destFactor().name() + "/" + a.sourceFactor().name() + "/" + a.destFactor().name()).toLowerCase(Locale.ROOT);
	}

	RenderPipeline GUI_DEPTH = RenderPipeline.builder()
		.withLocation(ID.vidlib("pipeline/gui_depth"))
		.withBindGroupLayout(BindGroupLayouts.GLOBALS)
		.withBindGroupLayout(BindGroupLayouts.PROJECTION)
		.withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
		.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, true))
		.withCull(false)
		.withVertexShader("core/position_tex_color")
		.withFragmentShader(ID.vidlib("core/gui_depth"))
		.withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
		.withPrimitiveTopology(PrimitiveTopology.QUADS)
		.build();

	RenderPipeline MASKED_GUI = RenderPipeline.builder()
		.withLocation(ID.vidlib("pipeline/masked_gui"))
		.withBindGroupLayout(BindGroupLayouts.GLOBALS)
		.withBindGroupLayout(BindGroupLayouts.PROJECTION)
		.withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
		.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
		.withBindGroupLayout(BindGroupLayouts.SAMPLER1)
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, true))
		.withCull(false)
		.withVertexShader("core/position_tex_color")
		.withFragmentShader(ID.vidlib("core/masked_gui"))
		.withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
		.withPrimitiveTopology(PrimitiveTopology.QUADS)
		.build();

	RenderPipeline MSDF = RenderPipeline.builder()
		.withLocation(ID.vidlib("pipeline/msdf"))
		.withBindGroupLayout(BindGroupLayouts.GLOBALS)
		.withBindGroupLayout(BindGroupLayouts.PROJECTION)
		.withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
		.withBindGroupLayout(BindGroupLayouts.FOG)
		.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, true))
		.withCull(true)
		.withVertexShader(ID.vidlib("core/msdf"))
		.withFragmentShader(ID.vidlib("core/msdf"))
		.withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
		.withPrimitiveTopology(PrimitiveTopology.QUADS)
		.build();

	RenderPipeline MSDF_SEE_THROUGH = RenderPipeline.builder()
		.withLocation(ID.vidlib("pipeline/msdf_see_through"))
		.withBindGroupLayout(BindGroupLayouts.GLOBALS)
		.withBindGroupLayout(BindGroupLayouts.PROJECTION)
		.withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
		.withBindGroupLayout(BindGroupLayouts.FOG)
		.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.withDepthStencilState(java.util.Optional.empty())
		.withCull(true)
		.withVertexShader(ID.vidlib("core/msdf"))
		.withFragmentShader(ID.vidlib("core/msdf"))
		.withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
		.withPrimitiveTopology(PrimitiveTopology.QUADS)
		.build();

	RenderPipeline SKYBOX = RenderPipeline.builder()
		.withLocation(ID.vidlib("pipeline/skybox"))
		.withBindGroupLayout(BindGroupLayouts.GLOBALS)
		.withBindGroupLayout(BindGroupLayouts.PROJECTION)
		.withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
		.withBindGroupLayout(BindGroupLayouts.FOG)
		.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
		.withColorTargetState(ColorTargetState.DEFAULT)
		.withCull(true)
		.withVertexShader("core/position_tex_color")
		.withFragmentShader("core/position_tex_color")
		.withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
		.withPrimitiveTopology(PrimitiveTopology.QUADS)
		.build();

	RenderPipeline SOLID_TERRAIN_NO_CULL = RenderPipeline.builder()
		.withLocation(ID.vidlib("pipeline/terrain/solid_no_cull"))
		.withBindGroupLayout(BindGroupLayouts.GLOBALS)
		.withBindGroupLayout(BindGroupLayouts.PROJECTION)
		.withBindGroupLayout(BindGroupLayouts.TERRAIN_INFO)
		.withBindGroupLayout(BindGroupLayouts.FOG)
		.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
		.withBindGroupLayout(BindGroupLayouts.SAMPLER2)
		.withBindGroupLayout(BindGroupLayouts.CHUNK_SECTION)
		.withColorTargetState(ColorTargetState.DEFAULT)
		.withDepthStencilState(DepthStencilState.DEFAULT)
		.withCull(false)
		.withVertexShader("core/terrain")
		.withFragmentShader("core/terrain")
		.withVertexBinding(0, DefaultVertexFormat.BLOCK)
		.withPrimitiveTopology(PrimitiveTopology.QUADS)
		.build();

	RenderPipeline CUTOUT_MIPPED_TERRAIN_NO_CULL = RenderPipeline.builder()
		.withLocation(ID.vidlib("pipeline/terrain/cutout_mipped_no_cull"))
		.withBindGroupLayout(BindGroupLayouts.GLOBALS)
		.withBindGroupLayout(BindGroupLayouts.PROJECTION)
		.withBindGroupLayout(BindGroupLayouts.TERRAIN_INFO)
		.withBindGroupLayout(BindGroupLayouts.FOG)
		.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
		.withBindGroupLayout(BindGroupLayouts.SAMPLER2)
		.withBindGroupLayout(BindGroupLayouts.CHUNK_SECTION)
		.withShaderDefine("ALPHA_CUTOUT", 0.5F)
		.withColorTargetState(ColorTargetState.DEFAULT)
		.withDepthStencilState(DepthStencilState.DEFAULT)
		.withCull(false)
		.withVertexShader("core/terrain")
		.withFragmentShader("core/terrain")
		.withVertexBinding(0, DefaultVertexFormat.BLOCK)
		.withPrimitiveTopology(PrimitiveTopology.QUADS)
		.build();

	RenderPipeline CUTOUT_TERRAIN_NO_CULL = RenderPipeline.builder()
		.withLocation(ID.vidlib("pipeline/terrain/cutout_no_cull"))
		.withBindGroupLayout(BindGroupLayouts.GLOBALS)
		.withBindGroupLayout(BindGroupLayouts.PROJECTION)
		.withBindGroupLayout(BindGroupLayouts.TERRAIN_INFO)
		.withBindGroupLayout(BindGroupLayouts.FOG)
		.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
		.withBindGroupLayout(BindGroupLayouts.SAMPLER2)
		.withBindGroupLayout(BindGroupLayouts.CHUNK_SECTION)
		.withShaderDefine("ALPHA_CUTOUT", 0.1F)
		.withColorTargetState(ColorTargetState.DEFAULT)
		.withDepthStencilState(DepthStencilState.DEFAULT)
		.withCull(false)
		.withVertexShader("core/terrain")
		.withFragmentShader("core/terrain")
		.withVertexBinding(0, DefaultVertexFormat.BLOCK)
		.withPrimitiveTopology(PrimitiveTopology.QUADS)
		.build();

	RenderPipeline TRANSLUCENT_TERRAIN_NO_CULL = RenderPipeline.builder()
		.withLocation(ID.vidlib("pipeline/terrain/translucent_no_cull"))
		.withBindGroupLayout(BindGroupLayouts.GLOBALS)
		.withBindGroupLayout(BindGroupLayouts.PROJECTION)
		.withBindGroupLayout(BindGroupLayouts.TERRAIN_INFO)
		.withBindGroupLayout(BindGroupLayouts.FOG)
		.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
		.withBindGroupLayout(BindGroupLayouts.SAMPLER2)
		.withBindGroupLayout(BindGroupLayouts.CHUNK_SECTION)
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.withDepthStencilState(DepthStencilState.DEFAULT)
		.withCull(false)
		.withVertexShader("core/terrain")
		.withFragmentShader("core/terrain")
		.withVertexBinding(0, DefaultVertexFormat.BLOCK)
		.withPrimitiveTopology(PrimitiveTopology.QUADS)
		.build();

	RenderPipeline ADDITIVE_PARTICLE = RenderPipeline.builder()
		.withLocation(ID.vidlib("pipeline/particle/additive"))
		.withBindGroupLayout(BindGroupLayouts.GLOBALS)
		.withBindGroupLayout(BindGroupLayouts.PROJECTION)
		.withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
		.withBindGroupLayout(BindGroupLayouts.FOG)
		.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
		.withBindGroupLayout(BindGroupLayouts.SAMPLER2)
		.withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
		.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
		.withVertexShader("core/particle")
		.withFragmentShader("core/particle")
		.withVertexBinding(0, DefaultVertexFormat.PARTICLE)
		.withPrimitiveTopology(PrimitiveTopology.QUADS)
		.build();

	RenderPipeline ADDITIVE_PARTICLE_ONLY_DEPTH = RenderPipeline.builder()
		.withLocation(ID.vidlib("pipeline/particle/additive_only_depth"))
		.withBindGroupLayout(BindGroupLayouts.GLOBALS)
		.withBindGroupLayout(BindGroupLayouts.PROJECTION)
		.withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
		.withBindGroupLayout(BindGroupLayouts.FOG)
		.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
		.withBindGroupLayout(BindGroupLayouts.SAMPLER2)
		.withColorTargetState(new ColorTargetState(java.util.Optional.of(BlendFunction.ADDITIVE), com.mojang.renderpearl.api.GpuFormat.RGBA8_UNORM, ColorTargetState.WRITE_NONE))
		.withDepthStencilState(DepthStencilState.DEFAULT)
		.withVertexShader("core/particle")
		.withFragmentShader("core/particle")
		.withVertexBinding(0, DefaultVertexFormat.PARTICLE)
		.withPrimitiveTopology(PrimitiveTopology.QUADS)
		.build();

	Function<BlendFunction, RenderPipeline> CANVAS_PIPELINES = Util.memoize(blendFunction -> RenderPipeline.builder()
		.withLocation(ID.vidlib("pipeline/canvas/" + blendName(blendFunction)))
		.withBindGroupLayout(BindGroupLayouts.GLOBALS)
		// 26.3 has no core/blit_screen VERTEX source ("Couldn't find source for VERTEX shader"):
		// vanilla blits use core/screenquad for the vertex stage and blit_screen only for the
		// fragment stage, drawn as a fullscreen 3-vertex triangle with no vertex buffer attached.
		.withBindGroupLayout(BindGroupLayouts.IN_SAMPLER)
		.withColorTargetState(new ColorTargetState(blendFunction))
		.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
		.withCull(false)
		.withVertexShader("core/screenquad")
		.withFragmentShader("core/blit_screen")
		.withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
		.build()
	);

	RenderPipeline.Snippet OUTLINE_SNIPPET = RenderPipeline.builder()
		.withBindGroupLayout(BindGroupLayouts.GLOBALS)
		.withBindGroupLayout(BindGroupLayouts.PROJECTION)
		.withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
		.withBindGroupLayout(BindGroupLayouts.FOG)
		.withVertexShader("core/rendertype_outline")
		.withFragmentShader("core/rendertype_outline")
		.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.withDepthStencilState(DepthStencilState.DEFAULT)
		.withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
		.withPrimitiveTopology(PrimitiveTopology.QUADS)
		.buildSnippet();

	RenderPipeline OUTLINE_CULL = RenderPipeline.builder(OUTLINE_SNIPPET).withLocation("pipeline/outline_cull").build();
	RenderPipeline OUTLINE_NO_CULL = RenderPipeline.builder(OUTLINE_SNIPPET).withLocation("pipeline/outline_no_cull").withCull(false).build();

	RenderPipeline STONE_ENTITY_NO_CULL = RenderPipeline.builder()
		.withLocation(ID.vidlib("pipeline/canvas/no_cull/stone_entity"))
		.withBindGroupLayout(BindGroupLayouts.GLOBALS)
		.withBindGroupLayout(BindGroupLayouts.PROJECTION)
		.withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
		.withBindGroupLayout(BindGroupLayouts.LIGHTING)
		.withBindGroupLayout(BindGroupLayouts.FOG)
		.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
		.withBindGroupLayout(BindGroupLayouts.SAMPLER1)
		.withBindGroupLayout(BindGroupLayouts.SAMPLER2)
		.withColorTargetState(ColorTargetState.DEFAULT)
		.withDepthStencilState(DepthStencilState.DEFAULT)
		.withCull(false)
		.withVertexShader("core/entity")
		.withFragmentShader(ID.vidlib("core/stone_entity"))
		.withVertexBinding(0, DefaultVertexFormat.ENTITY)
		.withPrimitiveTopology(PrimitiveTopology.QUADS)
		.build();

	@SubscribeEvent
	static void registerRenderPipelines(RegisterRenderPipelinesEvent event) {
		event.registerPipeline(GUI_DEPTH);
		event.registerPipeline(MASKED_GUI);
		event.registerPipeline(MSDF);
		event.registerPipeline(MSDF_SEE_THROUGH);
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
