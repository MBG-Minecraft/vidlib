package dev.beast.mods.shimmer.util.client;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.Empty;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;

import java.util.function.Function;

public interface ShimmerRenderTypes {
	RenderType WHITE_ENTITY = RenderType.entitySolid(Empty.TEXTURE);
	RenderType WHITE_TRANSLUCENT_ENTITY = RenderType.itemEntityTranslucentCull(Empty.TEXTURE);
	RenderType WHITE_TRANSLUCENT_NO_CULL_ENTITY = RenderType.entityTranslucent(Empty.TEXTURE);

	Function<ResourceLocation, RenderType> GUI = Util.memoize(
		texture -> RenderType.create(
			Shimmer.id("gui").toString(),
			786432,
			RenderPipelines.GUI_TEXTURED,
			RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
				.createCompositeState(false)
		)
	);

	Function<ResourceLocation, RenderType> SHIMMER_ENTITY_CUTOUT = Util.memoize(texture -> RenderType.create(Shimmer.id("entity_cutout").toString(),
		1536,
		true,
		false,
		RenderPipelines.ENTITY_CUTOUT,
		RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.setLightmapState(RenderStateShard.LIGHTMAP)
			.setOverlayState(RenderStateShard.OVERLAY)
			.createCompositeState(true)
	));

	Function<ResourceLocation, RenderType> SHIMMER_ENTITY_TRANSLUCENT_CULL = Util.memoize(texture -> RenderType.create(Shimmer.id("entity_translucent_cull").toString(),
		1536,
		true,
		true,
		RenderPipelines.ITEM_ENTITY_TRANSLUCENT_CULL,
		RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.setLightmapState(RenderStateShard.LIGHTMAP)
			.setOverlayState(RenderStateShard.OVERLAY)
			.createCompositeState(true)
	));

	Function<ResourceLocation, RenderType> SKYBOX = Util.memoize(texture -> RenderType.create(Shimmer.id("skybox").toString(),
		1536,
		true,
		true,
		ShimmerRenderPipelines.SKYBOX,
		RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, false))
			.createCompositeState(true)
	));

	static RenderType entityTextureCull(ResourceLocation texture, boolean translucent) {
		return translucent ? SHIMMER_ENTITY_TRANSLUCENT_CULL.apply(texture) : SHIMMER_ENTITY_CUTOUT.apply(texture);
	}
}
