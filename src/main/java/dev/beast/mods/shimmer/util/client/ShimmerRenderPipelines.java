package dev.beast.mods.shimmer.util.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.particle.physics.PhysicsParticlesRenderTypes;
import net.minecraft.client.renderer.RenderPipelines;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public interface ShimmerRenderPipelines {
	RenderPipeline SKYBOX = RenderPipeline.builder(RenderPipelines.MATRICES_COLOR_SNIPPET)
		.withLocation(Shimmer.id("skybox"))
		.withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
		.withVertexShader("core/position_tex_color")
		.withFragmentShader("core/position_tex_color")
		.withSampler("Sampler0")
		.withCull(true)
		.build();

	@SubscribeEvent
	static void registerRenderPipelines(RegisterRenderPipelinesEvent event) {
		event.registerPipeline(SKYBOX);
		event.registerPipeline(PhysicsParticlesRenderTypes.SOLID_PIPELINE);
		event.registerPipeline(PhysicsParticlesRenderTypes.CUTOUT_PIPELINE);
		event.registerPipeline(PhysicsParticlesRenderTypes.TRANSLUCENT_PIPELINE);
	}
}
