package dev.beast.mods.shimmer.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;

import java.util.Optional;
import java.util.function.Function;

public interface ShimmerRenderTypes {
	RenderType WHITE_ENTITY = RenderType.entitySolid(Empty.TEXTURE);
	RenderType WHITE_TRANSLUCENT_ENTITY = RenderType.itemEntityTranslucentCull(Empty.TEXTURE);
	RenderType WHITE_TRANSLUCENT_NO_CULL_ENTITY = RenderType.entityTranslucent(Empty.TEXTURE);

	class AutoTextureStateShard extends RenderStateShard.EmptyTextureStateShard {
		private final Optional<ResourceLocation> texture;

		public AutoTextureStateShard(ResourceLocation texture) {
			super(() -> {
				// var texturemanager = Minecraft.getInstance().getTextureManager();
				// texturemanager.getTexture(texture).setFilter(true, false);
				RenderSystem.setShaderTexture(0, texture);
			}, () -> {
			});
			this.texture = Optional.of(texture);
		}

		@Override
		public String toString() {
			return this.name + "[" + this.texture + "]";
		}

		@Override
		protected Optional<ResourceLocation> cutoutTexture() {
			return this.texture;
		}
	}

	Function<ResourceLocation, RenderType> GUI = Util.memoize(
		texture -> RenderType.create(
			"shimmer:gui",
			DefaultVertexFormat.POSITION_TEX_COLOR,
			VertexFormat.Mode.QUADS,
			786432,
			RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
				.setShaderState(RenderStateShard.POSITION_TEXTURE_COLOR_SHADER)
				.setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
				.setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
				.createCompositeState(false)
		)
	);

	Function<ResourceLocation, RenderType> SHIMMER_ENTITY_CUTOUT = Util.memoize(texture -> RenderType.create("shimmer:entity_cutout",
		DefaultVertexFormat.NEW_ENTITY,
		VertexFormat.Mode.QUADS,
		1536,
		true,
		false,
		RenderType.CompositeState.builder()
			.setShaderState(RenderStateShard.RENDERTYPE_ENTITY_CUTOUT_SHADER)
			.setTextureState(new AutoTextureStateShard(texture))
			.setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
			.setLightmapState(RenderStateShard.LIGHTMAP)
			.setOverlayState(RenderStateShard.OVERLAY)
			.createCompositeState(true)
	));

	Function<ResourceLocation, RenderType> SHIMMER_ENTITY_TRANSLUCENT_CULL = Util.memoize(texture -> RenderType.create("shimmer:entity_translucent_cull",
		DefaultVertexFormat.NEW_ENTITY,
		VertexFormat.Mode.QUADS,
		1536,
		true,
		true,
		RenderType.CompositeState.builder()
			.setShaderState(RenderStateShard.RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL_SHADER)
			.setTextureState(new AutoTextureStateShard(texture))
			.setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
			.setLightmapState(RenderStateShard.LIGHTMAP)
			.setOverlayState(RenderStateShard.OVERLAY)
			.createCompositeState(true)
	));

	Function<ResourceLocation, RenderType> SKYBOX = Util.memoize(texture -> RenderType.create("shimmer:skybox",
		DefaultVertexFormat.POSITION_TEX_COLOR,
		VertexFormat.Mode.QUADS,
		1536,
		true,
		true,
		RenderType.CompositeState.builder()
			.setShaderState(RenderStateShard.POSITION_TEXTURE_COLOR_SHADER)
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, false))
			.createCompositeState(true)
	));

	static RenderType entityTextureCull(ResourceLocation texture, boolean translucent) {
		return translucent ? SHIMMER_ENTITY_TRANSLUCENT_CULL.apply(texture) : SHIMMER_ENTITY_CUTOUT.apply(texture);
	}
}
