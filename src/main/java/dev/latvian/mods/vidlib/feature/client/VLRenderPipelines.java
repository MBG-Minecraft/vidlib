package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.pipeline.BlendEquation;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

@EventBusSubscriber(modid = VidLib.ID, value = Dist.CLIENT)
public interface VLRenderPipelines {
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
		.withColorTargetState(new ColorTargetState(Optional.of(BlendFunction.ADDITIVE), GpuFormat.RGBA8_UNORM, ColorTargetState.WRITE_NONE))
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

	RenderPipeline STONE_ENTITY_CUTOUT = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
		.withLocation(ID.vidlib("pipeline/stone_entity_cutout"))
		.withFragmentShader(ID.vidlib("core/stone_entity"))
		.withShaderDefine("ALPHA_CUTOUT", 0.1F)
		.withShaderDefine("PER_FACE_LIGHTING")
		.withBindGroupLayout(BindGroupLayouts.SAMPLER1)
		.withCull(false)
		.withColorTargetState(ColorTargetState.DEFAULT)
		.build();

	@SubscribeEvent
	static void registerRenderPipelines(RegisterRenderPipelinesEvent event) {
		event.registerPipeline(GUI_DEPTH);
		event.registerPipeline(MASKED_GUI);
		event.registerPipeline(SKYBOX);
		event.registerPipeline(SOLID_TERRAIN_NO_CULL);
		event.registerPipeline(CUTOUT_MIPPED_TERRAIN_NO_CULL);
		event.registerPipeline(CUTOUT_TERRAIN_NO_CULL);
		event.registerPipeline(TRANSLUCENT_TERRAIN_NO_CULL);
		event.registerPipeline(ADDITIVE_PARTICLE);
		event.registerPipeline(ADDITIVE_PARTICLE_ONLY_DEPTH);
		event.registerPipeline(STONE_ENTITY_CUTOUT);
	}
}
