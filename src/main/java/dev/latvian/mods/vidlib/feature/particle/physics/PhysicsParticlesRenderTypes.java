package dev.latvian.mods.vidlib.feature.particle.physics;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public interface PhysicsParticlesRenderTypes {
	VertexFormat FORMAT = VertexFormat.builder()
		.add("Position", VertexFormatElement.POSITION)
		.add("UV0", VertexFormatElement.UV0)
		.add("Normal", VertexFormatElement.NORMAL)
		.build();

	RenderPipeline.Snippet PIPELINE_BASE = RenderPipeline.builder(RenderPipelines.MATRICES_COLOR_SNIPPET)
		.withVertexFormat(FORMAT, VertexFormat.Mode.QUADS)
		.withVertexShader(VidLib.id("core/physics_particle"))
		.withFragmentShader(VidLib.id("core/physics_particle"))
		.withSampler("Sampler0")
		.withCull(true)
		.buildSnippet();

	RenderPipeline SOLID_PIPELINE = RenderPipeline.builder(PIPELINE_BASE)
		.withLocation(VidLib.id("pipeline/physics_particle/solid"))
		.build();

	RenderPipeline CUTOUT_PIPELINE = RenderPipeline.builder(PIPELINE_BASE)
		.withLocation(VidLib.id("pipeline/physics_particle/cutout"))
		.withShaderDefine("ALPHA_CUTOUT", 0.1F)
		.build();

	RenderPipeline TRANSLUCENT_PIPELINE = RenderPipeline.builder(PIPELINE_BASE)
		.withLocation(VidLib.id("pipeline/physics_particle/translucent"))
		.withShaderDefine("ALPHA_CUTOUT", 0.1F)
		.withBlend(BlendFunction.TRANSLUCENT)
		.build();

	RenderType PHYSICS_SOLID = RenderType.create(
		VidLib.id("physics_particle/solid").toString(),
		1536,
		SOLID_PIPELINE,
		RenderType.CompositeState.builder()
			.createCompositeState(false)
	);

	RenderType PHYSICS_CUTOUT = RenderType.create(
		VidLib.id("physics_particle/cutout").toString(),
		1536,
		CUTOUT_PIPELINE,
		RenderType.CompositeState.builder()
			.createCompositeState(false)
	);

	RenderType PHYSICS_TRANSLUCENT = RenderType.create(
		VidLib.id("physics_particle/translucent").toString(),
		1536,
		TRANSLUCENT_PIPELINE,
		RenderType.CompositeState.builder()
			.setOutputState(RenderStateShard.TRANSLUCENT_TARGET)
			.createCompositeState(false)
	);
}
