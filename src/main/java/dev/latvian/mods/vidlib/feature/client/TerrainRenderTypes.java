package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.vidlib.util.TerrainRenderLayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;

public interface TerrainRenderTypes {
	TexturedRenderType SOLID = terrain("terrain/solid", 4194304, false, RenderPipelines.SOLID_TERRAIN, false);
	TexturedRenderType SOLID_NO_CULL = terrain("terrain/solid_no_cull", 4194304, false, VidLibRenderPipelines.SOLID_TERRAIN_NO_CULL, false);
	TexturedRenderType CUTOUT_MIPPED = terrain("terrain/cutout_mipped", 4194304, false, RenderPipelines.CUTOUT_TERRAIN, false);
	TexturedRenderType CUTOUT_MIPPED_NO_CULL = terrain("terrain/cutout_mipped_no_cull", 4194304, false, VidLibRenderPipelines.CUTOUT_MIPPED_TERRAIN_NO_CULL, false);
	TexturedRenderType CUTOUT = terrain("terrain/cutout", 786432, false, RenderPipelines.CUTOUT_TERRAIN, false);
	TexturedRenderType CUTOUT_NO_CULL = terrain("terrain/cutout_no_cull", 786432, false, VidLibRenderPipelines.CUTOUT_TERRAIN_NO_CULL, false);
	TexturedRenderType TRANSLUCENT = terrain("terrain/translucent", 1536, true, RenderPipelines.TRANSLUCENT_TERRAIN, true);
	TexturedRenderType TRANSLUCENT_NO_CULL = terrain("terrain/translucent_no_cull", 1536, true, VidLibRenderPipelines.TRANSLUCENT_TERRAIN_NO_CULL, true);

	static TexturedRenderType get(TerrainRenderLayer type, boolean cull) {
		return (TexturedRenderType) (cull ? type.renderTypeFunction : type.noCullRenderTypeFunction);
	}

	private static TexturedRenderType terrain(String name, int bufferSize, boolean translucentTarget, com.mojang.blaze3d.pipeline.RenderPipeline pipeline, boolean sortOnUpload) {
		return TexturedRenderType.internal(
			name,
			bufferSize,
			true,
			sortOnUpload,
			texture -> {
				var builder = TexturedRenderType.textured(pipeline, texture, true, false)
					.setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE);

				if (translucentTarget) {
					builder.setOutputTarget(TexturedRenderType.TRANSLUCENT_TARGET);
				}

				return builder;
			}
		);
	}
}
