package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.vidlib.util.Empty;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;
import net.minecraft.world.level.material.FluidState;

public interface VidLibRenderTypes {
	TexturedRenderType GUI = TexturedRenderType.internal(
		"gui",
		786432,
		RenderPipelines.GUI_TEXTURED,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.createCompositeState(false)
	);

	TexturedRenderType SKYBOX = TexturedRenderType.internal(
		"skybox",
		1536,
		true,
		true,
		VidLibRenderPipelines.SKYBOX,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, false))
			.createCompositeState(true)
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

	interface Terrain {
		TexturedRenderType SOLID = TexturedRenderType.internal(
			"terrain/solid",
			1536,
			true,
			true,
			RenderPipelines.SOLID,
			texture -> RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, true))
				.setLightmapState(RenderStateShard.LIGHTMAP)
				.createCompositeState(true)
		);

		TexturedRenderType SOLID_NO_CULL = TexturedRenderType.internal(
			"terrain/solid_no_cull",
			1536,
			true,
			true,
			RenderPipelines.SOLID,
			texture -> RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, true))
				.setLightmapState(RenderStateShard.LIGHTMAP)
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
			RenderPipelines.TRANSLUCENT,
			texture -> RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, true))
				.setLightmapState(RenderStateShard.LIGHTMAP)
				.setOutputState(RenderStateShard.TRANSLUCENT_TARGET)
				.createCompositeState(true)
		);

		static TexturedRenderType get(boolean cull, int type) {
			return switch (type) {
				case 1 -> cull ? SOLID : SOLID_NO_CULL; // CUTOUT
				case 2 -> cull ? TRANSLUCENT : TRANSLUCENT_NO_CULL;
				default -> cull ? SOLID : SOLID_NO_CULL;
			};
		}
	}

	static RenderType getFluid(FluidState fluidState, DynamicSpriteTexture texture, boolean cull) {
		var o = ItemBlockRenderTypes.getRenderLayer(fluidState);
		return Terrain.get(cull, o == RenderType.translucent() ? 2 : 0).apply(texture.resourceId());
	}
}
