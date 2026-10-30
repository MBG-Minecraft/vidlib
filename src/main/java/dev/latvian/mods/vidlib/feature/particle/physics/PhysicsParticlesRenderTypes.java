package dev.latvian.mods.vidlib.feature.particle.physics;

import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import dev.latvian.mods.klib.util.ID;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;

public interface PhysicsParticlesRenderTypes {
	VertexFormat FORMAT = VertexFormat.builder(0)
		.addAttribute("Position", com.mojang.renderpearl.api.GpuFormat.RGB32_FLOAT)
		.addAttribute("Color", com.mojang.renderpearl.api.GpuFormat.RGBA8_UNORM)
		.addAttribute("UV0", com.mojang.renderpearl.api.GpuFormat.RG32_FLOAT)
		.addAttribute("UV2", com.mojang.renderpearl.api.GpuFormat.RG16_SINT)
		.addAttribute("Normal", com.mojang.renderpearl.api.GpuFormat.RGBA8_SNORM)
		.build();

	RenderPipeline.Snippet PIPELINE_BASE = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
		.withVertexShader(ID.vidlib("core/physics_particle"))
		.withFragmentShader(ID.vidlib("core/physics_particle"))
		.withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER2)
		.withVertexBinding(0, FORMAT)
		.withPrimitiveTopology(com.mojang.renderpearl.api.pipeline.PrimitiveTopology.QUADS)
		.withCull(true)
		.buildSnippet();

	RenderPipeline SOLID_PIPELINE = RenderPipeline.builder(PIPELINE_BASE)
		.withLocation(ID.vidlib("pipeline/physics_particle/solid"))
		.build();

	RenderPipeline CUTOUT_MIPPED_PIPELINE = RenderPipeline.builder(PIPELINE_BASE)
		.withLocation(ID.vidlib("pipeline/physics_particle/cutout_mipped"))
		.withShaderDefine("ALPHA_CUTOUT", 1)
		.build();

	RenderPipeline CUTOUT_PIPELINE = RenderPipeline.builder(PIPELINE_BASE)
		.withLocation(ID.vidlib("pipeline/physics_particle/cutout"))
		.withShaderDefine("ALPHA_CUTOUT", 0)
		.build();

	RenderPipeline TRANSLUCENT_PIPELINE = RenderPipeline.builder(PIPELINE_BASE)
		.withLocation(ID.vidlib("pipeline/physics_particle/translucent"))
		.withShaderDefine("ALPHA_CUTOUT", 0)
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.build();
}
