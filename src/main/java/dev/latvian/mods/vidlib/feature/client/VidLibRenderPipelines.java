package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.bloom.BloomRenderTypes;
import dev.latvian.mods.vidlib.feature.canvas.CanvasRenderPipelines;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticlesRenderTypes;
import dev.latvian.mods.vidlib.util.TerrainRenderLayer;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.Locale;
import java.util.function.Function;

@EventBusSubscriber(modid = VidLib.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public interface VidLibRenderPipelines {
	RenderPipeline GUI_DEPTH = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
		.withLocation(VidLib.id("pipeline/gui_depth"))
		.withFragmentShader(VidLib.id("core/gui_depth"))
		.build();

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

	Function<BlendFunction, RenderPipeline> CANVAS_PIPELINES = Util.memoize(blendFunction -> RenderPipeline.builder()
		.withLocation(VidLib.id("pipeline/canvas/" + blendFunction.sourceColor().name().toLowerCase(Locale.ROOT) + "/" + blendFunction.destColor().name().toLowerCase(Locale.ROOT) + "/" + blendFunction.sourceAlpha().name().toLowerCase(Locale.ROOT) + "/" + blendFunction.destAlpha().name().toLowerCase(Locale.ROOT) + "/"))
		.withVertexShader("core/blit_screen")
		.withFragmentShader("core/blit_screen")
		.withSampler("InSampler")
		.withBlend(blendFunction)
		.withDepthWrite(false)
		.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
		.withColorWrite(true, false)
		.withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
		.build()
	);

	RenderPipeline.Snippet OUTLINE_SNIPPET = RenderPipeline.builder(RenderPipelines.MATRICES_COLOR_SNIPPET)
		.withVertexShader("core/rendertype_outline")
		.withFragmentShader("core/rendertype_outline")
		.withSampler("Sampler0")
		.withBlend(BlendFunction.TRANSLUCENT)
		.withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
		.buildSnippet();

	RenderPipeline OUTLINE_CULL = RenderPipeline.builder(OUTLINE_SNIPPET).withLocation("pipeline/outline_cull").build();
	RenderPipeline OUTLINE_NO_CULL = RenderPipeline.builder(OUTLINE_SNIPPET).withLocation("pipeline/outline_no_cull").withCull(false).build();

	@SubscribeEvent
	static void registerRenderPipelines(RegisterRenderPipelinesEvent event) {
		event.registerPipeline(GUI_DEPTH);
		event.registerPipeline(SKYBOX);
		event.registerPipeline(SOLID_TERRAIN_NO_CULL);
		event.registerPipeline(CUTOUT_MIPPED_TERRAIN_NO_CULL);
		event.registerPipeline(CUTOUT_TERRAIN_NO_CULL);
		event.registerPipeline(TRANSLUCENT_TERRAIN_NO_CULL);
		event.registerPipeline(ADDITIVE_PARTICLE);
		event.registerPipeline(PhysicsParticlesRenderTypes.SOLID_PIPELINE);
		event.registerPipeline(PhysicsParticlesRenderTypes.CUTOUT_PIPELINE);
		event.registerPipeline(PhysicsParticlesRenderTypes.TRANSLUCENT_PIPELINE);
		CanvasRenderPipelines.register(event);

		TerrainRenderLayer.SOLID.setClientValues(RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS, RenderType.solid(), TerrainRenderTypes.SOLID, TerrainRenderTypes.SOLID_NO_CULL);
		TerrainRenderLayer.CUTOUT_MIPPED.setClientValues(RenderLevelStageEvent.Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS, RenderType.cutoutMipped(), TerrainRenderTypes.CUTOUT_MIPPED, TerrainRenderTypes.CUTOUT_MIPPED_NO_CULL);
		TerrainRenderLayer.CUTOUT.setClientValues(RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS, RenderType.cutout(), TerrainRenderTypes.CUTOUT, TerrainRenderTypes.CUTOUT_NO_CULL);
		TerrainRenderLayer.TRANSLUCENT.setClientValues(RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS, RenderType.translucent(), TerrainRenderTypes.TRANSLUCENT, TerrainRenderTypes.TRANSLUCENT_NO_CULL);
		TerrainRenderLayer.TRIPWIRE.setClientValues(RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS, RenderType.tripwire(), TerrainRenderTypes.TRANSLUCENT, TerrainRenderTypes.TRANSLUCENT_NO_CULL);
		TerrainRenderLayer.PARTICLE.setClientValues(RenderLevelStageEvent.Stage.AFTER_PARTICLES, RenderType.translucent(), TerrainRenderTypes.TRANSLUCENT, TerrainRenderTypes.TRANSLUCENT_NO_CULL);
		TerrainRenderLayer.BRIGHT.setClientValues(null, RenderType.translucent(), BrightRenderTypes.POS_TEX_COL, BrightRenderTypes.POS_TEX_COL_NO_CULL);
		TerrainRenderLayer.BLOOM.setClientValues(null, RenderType.translucent(), BloomRenderTypes.POS_TEX_COL, BloomRenderTypes.POS_TEX_COL_NO_CULL);
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
