package dev.latvian.mods.vidlib.feature.particle.physics;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.client.TexturedRenderType;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;

public interface PhysicsParticlesRenderTypes {
	VertexFormat FORMAT = VertexFormat.builder()
		.add("Position", VertexFormatElement.POSITION)
		.add("Color", VertexFormatElement.COLOR)
		.add("UV0", VertexFormatElement.UV0)
		.add("UV2", VertexFormatElement.UV2)
		.add("Normal", VertexFormatElement.NORMAL)
		.padding(1)
		.build();

	RenderPipeline.Snippet PIPELINE_BASE = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
		.withVertexFormat(FORMAT, VertexFormat.Mode.QUADS)
		.withVertexShader(VidLib.id("core/physics_particle"))
		.withFragmentShader(VidLib.id("core/physics_particle"))
		.withSampler("Sampler0")
		.withSampler("Sampler2")
		.withCull(true)
		.buildSnippet();

	RenderPipeline SOLID_PIPELINE = RenderPipeline.builder(PIPELINE_BASE)
		.withLocation(VidLib.id("pipeline/physics_particle/solid"))
		.build();

	RenderType SOLID = RenderType.create(
		"physics_particle_solid",
		RenderSetup.builder(SOLID_PIPELINE)
			.withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
			.useLightmap()
			.bufferSize(1536)
			.setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
			.createRenderSetup()
	);

	RenderPipeline CUTOUT_MIPPED_PIPELINE = RenderPipeline.builder(PIPELINE_BASE)
		.withLocation(VidLib.id("pipeline/physics_particle/cutout_mipped"))
		.withShaderDefine("ALPHA_CUTOUT", 0.5F)
		.build();

	RenderType CUTOUT_MIPPED = RenderType.create(
		"physics_particle_cutout_mipped",
		RenderSetup.builder(CUTOUT_MIPPED_PIPELINE)
			.withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
			.useLightmap()
			.affectsCrumbling()
			.bufferSize(1536)
			.setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
			.createRenderSetup()
	);

	RenderPipeline CUTOUT_PIPELINE = RenderPipeline.builder(PIPELINE_BASE)
		.withLocation(VidLib.id("pipeline/physics_particle/cutout"))
		.withShaderDefine("ALPHA_CUTOUT", 0.1F)
		.build();

	RenderType CUTOUT = RenderType.create(
		"physics_particle_cutout",
		RenderSetup.builder(CUTOUT_PIPELINE)
			.withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
			.useLightmap()
			.bufferSize(1536)
			.setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
			.createRenderSetup()
	);

	RenderPipeline TRANSLUCENT_PIPELINE = RenderPipeline.builder(PIPELINE_BASE)
		.withLocation(VidLib.id("pipeline/physics_particle/translucent"))
		.withShaderDefine("ALPHA_CUTOUT", 0.1F)
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.build();

	RenderType TRANSLUCENT = RenderType.create(
		"physics_particle_translucent",
		RenderSetup.builder(TRANSLUCENT_PIPELINE)
			.withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
			.useLightmap()
			.bufferSize(1536)
			.sortOnUpload()
			.setOutputTarget(TexturedRenderType.TRANSLUCENT_TARGET)
			.setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
			.createRenderSetup()
	);
}
