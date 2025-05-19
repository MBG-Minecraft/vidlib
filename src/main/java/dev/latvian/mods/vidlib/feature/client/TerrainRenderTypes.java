package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.vidlib.util.TerrainRenderLayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.TriState;

public interface TerrainRenderTypes {
	TexturedRenderType SOLID = TexturedRenderType.internal(
		"terrain/solid",
		4194304,
		true,
		false,
		RenderPipelines.SOLID,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, true))
			.setLightmapState(RenderStateShard.LIGHTMAP)
			.createCompositeState(true)
	);

	TexturedRenderType SOLID_NO_CULL = TexturedRenderType.internal(
		"terrain/solid_no_cull",
		4194304,
		true,
		false,
		VidLibRenderPipelines.SOLID_TERRAIN_NO_CULL,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, true))
			.setLightmapState(RenderStateShard.LIGHTMAP)
			.createCompositeState(true)
	);

	TexturedRenderType CUTOUT_MIPPED = TexturedRenderType.internal(
		"terrain/cutout_mipped",
		4194304,
		true,
		false,
		RenderPipelines.CUTOUT_MIPPED,
		texture -> RenderType.CompositeState.builder()
			.setLightmapState(RenderStateShard.LIGHTMAP)
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, true))
			.createCompositeState(true)
	);

	TexturedRenderType CUTOUT_MIPPED_NO_CULL = TexturedRenderType.internal(
		"terrain/cutout_mipped_no_cull",
		4194304,
		true,
		false,
		VidLibRenderPipelines.CUTOUT_MIPPED_TERRAIN_NO_CULL,
		texture -> RenderType.CompositeState.builder()
			.setLightmapState(RenderStateShard.LIGHTMAP)
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, true))
			.createCompositeState(true)
	);

	TexturedRenderType CUTOUT = TexturedRenderType.internal(
		"terrain/cutout",
		786432,
		true,
		false,
		RenderPipelines.CUTOUT,
		texture -> RenderType.CompositeState.builder()
			.setLightmapState(RenderStateShard.LIGHTMAP)
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, false))
			.createCompositeState(true)
	);

	TexturedRenderType CUTOUT_NO_CULL = TexturedRenderType.internal(
		"terrain/cutout_no_cull",
		786432,
		true,
		false,
		VidLibRenderPipelines.CUTOUT_TERRAIN_NO_CULL,
		texture -> RenderType.CompositeState.builder()
			.setLightmapState(RenderStateShard.LIGHTMAP)
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, false))
			.createCompositeState(true)
	);

	TexturedRenderType TRANSLUCENT = TexturedRenderType.internal(
		"terrain/translucent",
		1536,
		true,
		true,
		RenderPipelines.TRANSLUCENT,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, true))
			.setLightmapState(RenderStateShard.LIGHTMAP)
			.setOutputState(RenderStateShard.TRANSLUCENT_TARGET)
			.createCompositeState(true)
	);

	TexturedRenderType TRANSLUCENT_NO_CULL = TexturedRenderType.internal(
		"terrain/translucent_no_cull",
		1536,
		true,
		true,
		VidLibRenderPipelines.TRANSLUCENT_TERRAIN_NO_CULL,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, true))
			.setLightmapState(RenderStateShard.LIGHTMAP)
			.setOutputState(RenderStateShard.TRANSLUCENT_TARGET)
			.createCompositeState(true)
	);

	static TexturedRenderType get(TerrainRenderLayer type, boolean cull) {
		return (TexturedRenderType) (cull ? type.renderTypeFunction : type.noCullRenderTypeFunction);
	}
}