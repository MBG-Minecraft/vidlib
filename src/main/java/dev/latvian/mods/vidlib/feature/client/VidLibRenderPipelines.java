package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticlesRenderTypes;
import net.minecraft.client.renderer.RenderPipelines;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

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

	RenderPipeline TRANSLUCENT_TERRAIN_NO_CULL = RenderPipeline.builder(RenderPipelines.TERRAIN_SNIPPET)
		.withLocation(VidLib.id("pipeline/terrain/translucent_no_cull"))
		.withBlend(BlendFunction.TRANSLUCENT)
		.withCull(false)
		.build();

	@SubscribeEvent
	static void registerRenderPipelines(RegisterRenderPipelinesEvent event) {
		event.registerPipeline(SKYBOX);
		event.registerPipeline(SOLID_TERRAIN_NO_CULL);
		event.registerPipeline(TRANSLUCENT_TERRAIN_NO_CULL);
		event.registerPipeline(PhysicsParticlesRenderTypes.SOLID_PIPELINE);
		event.registerPipeline(PhysicsParticlesRenderTypes.CUTOUT_PIPELINE);
		event.registerPipeline(PhysicsParticlesRenderTypes.TRANSLUCENT_PIPELINE);
	}
}
