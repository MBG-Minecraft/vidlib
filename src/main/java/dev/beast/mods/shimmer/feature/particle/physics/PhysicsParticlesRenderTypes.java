package dev.beast.mods.shimmer.feature.particle.physics;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import dev.beast.mods.shimmer.Shimmer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public interface PhysicsParticlesRenderTypes {
	VertexFormat FORMAT = VertexFormat.builder()
		.add("Position", VertexFormatElement.POSITION)
		.add("UV0", VertexFormatElement.UV0)
		.add("Normal", VertexFormatElement.NORMAL)
		.build();

	ResourceLocation SOLID_ID = Shimmer.id("physics_particle/solid");
	ResourceLocation CUTOUT_ID = Shimmer.id("physics_particle/cutout");
	ResourceLocation TRANSLUCENT_ID = Shimmer.id("physics_particle/translucent");
	ResourceLocation VERTEX_ID = Shimmer.id("physics_particle/vertex");

	RenderPipeline.Snippet PIPELINE_BASE = RenderPipeline.builder(RenderPipelines.MATRICES_COLOR_SNIPPET)
		.withVertexFormat(FORMAT, VertexFormat.Mode.QUADS)
		.withSampler("Sampler0")
		.withCull(true)
		.buildSnippet();

	RenderPipeline SOLID_PIPELINE = RenderPipeline.builder(PIPELINE_BASE)
		.withLocation(SOLID_ID)
		.withVertexShader(VERTEX_ID)
		.withFragmentShader(SOLID_ID)
		.build();

	RenderPipeline CUTOUT_PIPELINE = RenderPipeline.builder(PIPELINE_BASE)
		.withLocation(CUTOUT_ID)
		.withVertexShader(VERTEX_ID)
		.withFragmentShader(CUTOUT_ID)
		.withShaderDefine("ALPHA_CUTOUT", 0.1F)
		.build();

	RenderPipeline TRANSLUCENT_PIPELINE = RenderPipeline.builder(PIPELINE_BASE)
		.withLocation(TRANSLUCENT_ID)
		.withVertexShader(VERTEX_ID)
		.withFragmentShader(TRANSLUCENT_ID)
		.withShaderDefine("ALPHA_CUTOUT", 0.1F)
		.withBlend(BlendFunction.TRANSLUCENT)
		.build();

	RenderType PHYSICS_SOLID = RenderType.create(
		SOLID_ID.toString(),
		1536,
		SOLID_PIPELINE,
		RenderType.CompositeState.builder()
			.createCompositeState(false)
	);

	RenderType PHYSICS_CUTOUT = RenderType.create(
		CUTOUT_ID.toString(),
		1536,
		CUTOUT_PIPELINE,
		RenderType.CompositeState.builder()
			.createCompositeState(false)
	);

	RenderType PHYSICS_TRANSLUCENT = RenderType.create(
		TRANSLUCENT_ID.toString(),
		1536,
		TRANSLUCENT_PIPELINE,
		RenderType.CompositeState.builder()
			.setOutputState(RenderStateShard.TRANSLUCENT_TARGET)
			.createCompositeState(false)
	);
}
