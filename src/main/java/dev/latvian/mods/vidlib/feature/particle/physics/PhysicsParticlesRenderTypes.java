package dev.latvian.mods.vidlib.feature.particle.physics;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.TriState;

public interface PhysicsParticlesRenderTypes {
	VertexFormat FORMAT = VertexFormat.builder()
		.add("Position", VertexFormatElement.POSITION)
		.add("UV0", VertexFormatElement.UV0)
		.add("UV2", VertexFormatElement.UV2)
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

	RenderType SOLID = RenderType.create(
		"physics_particle_solid",
		1536,
		false,
		false,
		SOLID_PIPELINE,
		RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_BLOCKS, TriState.FALSE, true))
			.setLightmapState(RenderStateShard.LIGHTMAP)
			.createCompositeState(true)
	);

	RenderPipeline CUTOUT_PIPELINE = RenderPipeline.builder(PIPELINE_BASE)
		.withLocation(VidLib.id("pipeline/physics_particle/cutout"))
		.withShaderDefine("ALPHA_CUTOUT", 0.1F)
		.build();

	RenderType CUTOUT = RenderType.create(
		"physics_particle_cutout",
		1536,
		false,
		false,
		CUTOUT_PIPELINE,
		RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_BLOCKS, TriState.FALSE, false))
			.setLightmapState(RenderStateShard.LIGHTMAP)
			.createCompositeState(true)
	);

	RenderPipeline TRANSLUCENT_PIPELINE = RenderPipeline.builder(PIPELINE_BASE)
		.withLocation(VidLib.id("pipeline/physics_particle/translucent"))
		.withShaderDefine("ALPHA_CUTOUT", 0.1F)
		.withBlend(BlendFunction.TRANSLUCENT)
		.build();

	RenderType TRANSLUCENT = RenderType.create(
		"physics_particle_translucent",
		1536,
		false,
		true,
		TRANSLUCENT_PIPELINE,
		RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_BLOCKS, TriState.FALSE, true))
			.setLightmapState(RenderStateShard.LIGHTMAP)
			.setOutputState(RenderStateShard.TRANSLUCENT_TARGET)
			.createCompositeState(true)
	);
}
