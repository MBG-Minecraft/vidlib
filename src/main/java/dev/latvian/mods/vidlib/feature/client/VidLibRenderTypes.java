package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.canvas.BossRendering;
import dev.latvian.mods.vidlib.feature.canvas.CanvasRenderPipelines;
import dev.latvian.mods.vidlib.util.Empty;
import dev.latvian.mods.vidlib.util.TerrainRenderLayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;

public interface VidLibRenderTypes {
	TexturedRenderType GUI = TexturedRenderType.internal(
		"gui",
		786432,
		RenderPipelines.GUI_TEXTURED,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.createCompositeState(false)
	);

	TexturedRenderType GUI_DEPTH = TexturedRenderType.internal(
		"gui_depth",
		786432,
		VidLibRenderPipelines.GUI_DEPTH,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.createCompositeState(false)
	);

	TexturedRenderType SKYBOX = TexturedRenderType.internal(
		"skybox",
		DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize() * 6,
		true,
		true,
		VidLibRenderPipelines.SKYBOX,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, false))
			.createCompositeState(false)
	);

	interface Entity {
		RenderType WHITE = RenderType.entitySolid(Empty.TEXTURE);
		RenderType WHITE_TRANSLUCENT = RenderType.itemEntityTranslucentCull(Empty.TEXTURE);
		RenderType WHITE_TRANSLUCENT_NO_CULL = RenderType.entityTranslucent(Empty.TEXTURE);

		TexturedRenderType CUTOUT = TexturedRenderType.internal(
			"entity/cutout",
			1536,
			true,
			false,
			RenderPipelines.ENTITY_CUTOUT,
			texture -> RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
				.setLightmapState(RenderStateShard.LIGHTMAP)
				.setOverlayState(RenderStateShard.OVERLAY)
				.createCompositeState(true)
		);

		TexturedRenderType CUTOUT_NO_CULL = TexturedRenderType.internal(
			"entity/cutout_no_cull",
			1536,
			true,
			false,
			RenderPipelines.ENTITY_CUTOUT_NO_CULL,
			texture -> RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
				.setLightmapState(RenderStateShard.LIGHTMAP)
				.setOverlayState(RenderStateShard.OVERLAY)
				.createCompositeState(true)
		);

		TexturedRenderType TRANSLUCENT = TexturedRenderType.internal(
			"entity/translucent",
			1536,
			true,
			true,
			RenderPipelines.ITEM_ENTITY_TRANSLUCENT_CULL,
			texture -> RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
				.setLightmapState(RenderStateShard.LIGHTMAP)
				.setOverlayState(RenderStateShard.OVERLAY)
				.createCompositeState(true)
		);

		TexturedRenderType TRANSLUCENT_NO_CULL = TexturedRenderType.internal(
			"entity/translucent_no_cull",
			1536,
			true,
			true,
			RenderPipelines.ENTITY_TRANSLUCENT,
			texture -> RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
				.setLightmapState(RenderStateShard.LIGHTMAP)
				.setOverlayState(RenderStateShard.OVERLAY)
				.createCompositeState(true)
		);

		static RenderType textureCull(ResourceLocation texture, boolean translucent) {
			return translucent ? TRANSLUCENT.apply(texture) : CUTOUT.apply(texture);
		}

		static RenderType texture(ResourceLocation texture, boolean translucent) {
			return translucent ? TRANSLUCENT_NO_CULL.apply(texture) : CUTOUT_NO_CULL.apply(texture);
		}
	}

	interface BossEntity {
		TexturedRenderType CULL = TexturedRenderType.internal(
			"boss/cull",
			1536,
			true,
			true,
			CanvasRenderPipelines.POS_TEX_COL,
			texture -> RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
				.setOutputState(BossRendering.CANVAS.getOutputStateShard())
				.createCompositeState(false)
		);

		TexturedRenderType NO_CULL = TexturedRenderType.internal(
			"boss/no_cull",
			1536,
			true,
			true,
			CanvasRenderPipelines.POS_TEX_COL_NO_CULL,
			texture -> RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
				.setOutputState(BossRendering.CANVAS.getOutputStateShard())
				.createCompositeState(false)
		);

		RenderType WHITE_CULL = CULL.apply(Empty.TEXTURE);
		RenderType WHITE_NO_CULL = NO_CULL.apply(Empty.TEXTURE);
	}

	interface Terrain {
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

	interface Particle {
		RenderType ADDITIVE = RenderType.create(
			VidLib.id("particle/additive").toString(),
			1536,
			false,
			false,
			VidLibRenderPipelines.ADDITIVE_PARTICLE,
			RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_PARTICLES, TriState.FALSE, false))
				.setLightmapState(RenderStateShard.LIGHTMAP)
				.setOutputState(RenderStateShard.PARTICLES_TARGET)
				.createCompositeState(false)
		);
	}
}
