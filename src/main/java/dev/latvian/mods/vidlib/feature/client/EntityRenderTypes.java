package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.klib.render.BufferSupplier;
import dev.latvian.mods.klib.util.Empty;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;

public interface EntityRenderTypes {
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

	BufferSupplier WHITE_CUTOUT_BUFFER_SUPPLIER = BufferSupplier.fixed(CUTOUT.apply(Empty.TEXTURE), CUTOUT_NO_CULL.apply(Empty.TEXTURE));

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

	BufferSupplier WHITE_TRANSLUCENT_BUFFER_SUPPLIER = BufferSupplier.fixed(TRANSLUCENT.apply(Empty.TEXTURE), TRANSLUCENT_NO_CULL.apply(Empty.TEXTURE));

	TexturedRenderType STONE_CUTOUT_NO_CULL = TexturedRenderType.internal(
		"entity/stone_cutout_no_cull",
		1536,
		true,
		false,
		VidLibRenderPipelines.STONE_ENTITY_NO_CULL,
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
