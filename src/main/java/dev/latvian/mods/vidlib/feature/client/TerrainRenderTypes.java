package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.vidlib.util.TerrainRenderLayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;

public interface TerrainRenderTypes {
	TexturedRenderType SOLID = TexturedRenderType.internal("terrain/solid", RenderPipelines.SOLID_TERRAIN, texture -> RenderSetup.builder(RenderPipelines.SOLID_TERRAIN)
		.withTexture("Sampler0", texture)
		.useLightmap()
		.createRenderSetup());

	TexturedRenderType SOLID_NO_CULL = TexturedRenderType.internal("terrain/solid_no_cull", VLRenderPipelines.SOLID_TERRAIN_NO_CULL, texture -> RenderSetup.builder(VLRenderPipelines.SOLID_TERRAIN_NO_CULL)
		.withTexture("Sampler0", texture)
		.useLightmap()
		.createRenderSetup());

	TexturedRenderType CUTOUT_MIPPED = TexturedRenderType.internal("terrain/cutout_mipped", RenderPipelines.CUTOUT_TERRAIN, texture -> RenderSetup.builder(RenderPipelines.CUTOUT_TERRAIN)
		.useLightmap()
		.withTexture("Sampler0", texture)
		.createRenderSetup());

	TexturedRenderType CUTOUT_MIPPED_NO_CULL = TexturedRenderType.internal("terrain/cutout_mipped_no_cull", VLRenderPipelines.CUTOUT_MIPPED_TERRAIN_NO_CULL, texture -> RenderSetup.builder(VLRenderPipelines.CUTOUT_MIPPED_TERRAIN_NO_CULL)
		.useLightmap()
		.withTexture("Sampler0", texture)
		.createRenderSetup());

	TexturedRenderType CUTOUT = TexturedRenderType.internal("terrain/cutout", RenderPipelines.CUTOUT_TERRAIN, texture -> RenderSetup.builder(RenderPipelines.CUTOUT_TERRAIN)
		.useLightmap()
		.withTexture("Sampler0", texture)
		.createRenderSetup());

	TexturedRenderType CUTOUT_NO_CULL = TexturedRenderType.internal("terrain/cutout_no_cull", VLRenderPipelines.CUTOUT_TERRAIN_NO_CULL, texture -> RenderSetup.builder(VLRenderPipelines.CUTOUT_TERRAIN_NO_CULL)
		.useLightmap()
		.withTexture("Sampler0", texture)
		.createRenderSetup());

	TexturedRenderType TRANSLUCENT = TexturedRenderType.internal("terrain/translucent", RenderPipelines.TRANSLUCENT_TERRAIN, texture -> RenderSetup.builder(RenderPipelines.TRANSLUCENT_TERRAIN)
		.withTexture("Sampler0", texture)
		.useLightmap()

		.createRenderSetup());

	TexturedRenderType TRANSLUCENT_NO_CULL = TexturedRenderType.internal("terrain/translucent_no_cull", VLRenderPipelines.TRANSLUCENT_TERRAIN_NO_CULL, texture -> RenderSetup.builder(VLRenderPipelines.TRANSLUCENT_TERRAIN_NO_CULL)
		.withTexture("Sampler0", texture)
		.useLightmap()

		.createRenderSetup());

	static TexturedRenderType get(TerrainRenderLayer type, boolean cull) {
		return (TexturedRenderType) (cull ? type.renderTypeFunction : type.noCullRenderTypeFunction);
	}
}