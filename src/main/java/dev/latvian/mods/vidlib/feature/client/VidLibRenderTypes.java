package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.util.Empty;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;

import java.util.function.Function;

public interface VidLibRenderTypes {
	RenderType WHITE_ENTITY = RenderType.entitySolid(Empty.TEXTURE);
	RenderType WHITE_TRANSLUCENT_ENTITY = RenderType.itemEntityTranslucentCull(Empty.TEXTURE);
	RenderType WHITE_TRANSLUCENT_NO_CULL_ENTITY = RenderType.entityTranslucent(Empty.TEXTURE);

	Function<ResourceLocation, RenderType> GUI = Util.memoize(
		texture -> RenderType.create(
			VidLib.id("gui").toString(),
			786432,
			RenderPipelines.GUI_TEXTURED,
			RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
				.createCompositeState(false)
		)
	);

	Function<ResourceLocation, RenderType> ENTITY_CUTOUT = Util.memoize(texture -> RenderType.create(VidLib.id("entity_cutout").toString(),
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

	Function<ResourceLocation, RenderType> ENTITY_TRANSLUCENT_CULL = Util.memoize(texture -> RenderType.create(VidLib.id("entity_translucent_cull").toString(),
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

	Function<ResourceLocation, RenderType> SKYBOX = Util.memoize(texture -> RenderType.create(VidLib.id("skybox").toString(),
		1536,
		true,
		true,
		VidLibRenderPipelines.SKYBOX,
		RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, false))
			.createCompositeState(true)
	));

	static RenderType entityTextureCull(ResourceLocation texture, boolean translucent) {
		return translucent ? ENTITY_TRANSLUCENT_CULL.apply(texture) : ENTITY_CUTOUT.apply(texture);
	}
}
