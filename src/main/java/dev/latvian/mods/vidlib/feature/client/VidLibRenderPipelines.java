package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticlesRenderTypes;
import dev.latvian.mods.vidlib.util.TerrainRenderLayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = VidLib.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public interface VidLibRenderPipelines {
	RenderPipeline SKYBOX = RenderPipeline.builder(RenderPipelines.MATRICES_COLOR_SNIPPET)
		.withLocation(VidLib.id("pipeline/skybox"))
		.withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
		.withVertexShader("core/position_tex_color")
		.withFragmentShader("core/position_tex_color")
		.withSampler("Sampler0")
		.withCull(true)
		.build();

	RenderPipeline SOLID_TERRAIN_NO_CULL = RenderPipeline.builder(RenderPipelines.TERRAIN_SNIPPET)
		.withLocation(VidLib.id("pipeline/terrain/solid_no_cull"))
		.withCull(false)
		.build();

	RenderPipeline CUTOUT_MIPPED_TERRAIN_NO_CULL = RenderPipeline.builder(RenderPipelines.TERRAIN_SNIPPET)
		.withLocation(VidLib.id("pipeline/terrain/cutout_mipped_no_cull"))
		.withShaderDefine("ALPHA_CUTOUT", 0.5F)
		.withCull(false)
		.build();

	RenderPipeline CUTOUT_TERRAIN_NO_CULL = RenderPipeline.builder(RenderPipelines.TERRAIN_SNIPPET)
		.withLocation(VidLib.id("pipeline/terrain/cutout_no_cull"))
		.withShaderDefine("ALPHA_CUTOUT", 0.1F)
		.withCull(false)
		.build();

	RenderPipeline TRANSLUCENT_TERRAIN_NO_CULL = RenderPipeline.builder(RenderPipelines.TERRAIN_SNIPPET)
		.withLocation(VidLib.id("pipeline/terrain/translucent_no_cull"))
		.withBlend(BlendFunction.TRANSLUCENT)
		.withCull(false)
		.build();

	RenderPipeline ADDITIVE_PARTICLE = RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
		.withLocation(VidLib.id("pipeline/particle/additive"))
		.withBlend(BlendFunction.ADDITIVE)
		// .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
		.withDepthWrite(false)
		.build();

	@SubscribeEvent
	static void registerRenderPipelines(RegisterRenderPipelinesEvent event) {
		event.registerPipeline(SKYBOX);
		event.registerPipeline(SOLID_TERRAIN_NO_CULL);
		event.registerPipeline(CUTOUT_MIPPED_TERRAIN_NO_CULL);
		event.registerPipeline(CUTOUT_TERRAIN_NO_CULL);
		event.registerPipeline(TRANSLUCENT_TERRAIN_NO_CULL);
		event.registerPipeline(ADDITIVE_PARTICLE);
		event.registerPipeline(PhysicsParticlesRenderTypes.SOLID_PIPELINE);
		event.registerPipeline(PhysicsParticlesRenderTypes.CUTOUT_PIPELINE);
		event.registerPipeline(PhysicsParticlesRenderTypes.TRANSLUCENT_PIPELINE);

		TerrainRenderLayer.SOLID.setClientValues(RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS, RenderType.solid(), VidLibRenderTypes.Terrain.SOLID, VidLibRenderTypes.Terrain.SOLID_NO_CULL);
		TerrainRenderLayer.CUTOUT_MIPPED.setClientValues(RenderLevelStageEvent.Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS, RenderType.cutoutMipped(), VidLibRenderTypes.Terrain.CUTOUT_MIPPED, VidLibRenderTypes.Terrain.CUTOUT_MIPPED_NO_CULL);
		TerrainRenderLayer.CUTOUT.setClientValues(RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS, RenderType.cutout(), VidLibRenderTypes.Terrain.CUTOUT, VidLibRenderTypes.Terrain.CUTOUT_NO_CULL);
		TerrainRenderLayer.TRANSLUCENT.setClientValues(RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS, RenderType.translucent(), VidLibRenderTypes.Terrain.TRANSLUCENT, VidLibRenderTypes.Terrain.TRANSLUCENT_NO_CULL);
		TerrainRenderLayer.TRIPWIRE.setClientValues(RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS, RenderType.tripwire(), VidLibRenderTypes.Terrain.TRANSLUCENT, VidLibRenderTypes.Terrain.TRANSLUCENT_NO_CULL);
		TerrainRenderLayer.PARTICLE.setClientValues(RenderLevelStageEvent.Stage.AFTER_PARTICLES, RenderType.translucent(), VidLibRenderTypes.Terrain.TRANSLUCENT, VidLibRenderTypes.Terrain.TRANSLUCENT_NO_CULL);
	}
}
