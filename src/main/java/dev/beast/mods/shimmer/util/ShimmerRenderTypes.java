package dev.beast.mods.shimmer.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;
import org.lwjgl.opengl.GL11;

import java.util.Optional;
import java.util.function.Function;

public class ShimmerRenderTypes extends RenderType {
	public static final RenderType WHITE_ENTITY = entitySolid(Empty.TEXTURE);
	public static final RenderType WHITE_TRANSLUCENT_ENTITY = itemEntityTranslucentCull(Empty.TEXTURE);
	public static final RenderType WHITE_TRANSLUCENT_NO_CULL_ENTITY = entityTranslucent(Empty.TEXTURE);

	public static class AutoTextureStateShard extends RenderStateShard.EmptyTextureStateShard {
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

	public static void setupLines() {
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
	}

	public static void clearLines() {
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
	}

	public static final RenderType DEBUG_LINES = create(
		"shimmer:debug_lines",
		DefaultVertexFormat.POSITION_COLOR,
		VertexFormat.Mode.DEBUG_LINES,
		1536,
		RenderType.CompositeState.builder()
			.setShaderState(POSITION_COLOR_SHADER)
			.setTextureState(new EmptyTextureStateShard(ShimmerRenderTypes::setupLines, ShimmerRenderTypes::clearLines))
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setCullState(CULL)
			.setLayeringState(VIEW_OFFSET_Z_LAYERING)
			.createCompositeState(false)
	);

	public static final RenderType DEBUG_QUADS = create(
		"shimmer:debug_quads",
		DefaultVertexFormat.POSITION_COLOR,
		VertexFormat.Mode.QUADS,
		1536,
		RenderType.CompositeState.builder()
			.setShaderState(POSITION_COLOR_SHADER)
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setCullState(CULL)
			.setDepthTestState(LEQUAL_DEPTH_TEST)
			.setWriteMaskState(COLOR_WRITE)
			.setLayeringState(VIEW_OFFSET_Z_LAYERING)
			.createCompositeState(false)
	);

	public static final RenderType DEBUG_QUADS_NO_CULL = create(
		"shimmer:debug_quads_no_cull",
		DefaultVertexFormat.POSITION_COLOR,
		VertexFormat.Mode.QUADS,
		1536,
		RenderType.CompositeState.builder()
			.setShaderState(POSITION_COLOR_SHADER)
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setCullState(NO_CULL)
			.setDepthTestState(LEQUAL_DEPTH_TEST)
			.setWriteMaskState(COLOR_WRITE)
			.setLayeringState(VIEW_OFFSET_Z_LAYERING)
			.createCompositeState(false)
	);

	public static final Function<ResourceLocation, RenderType> SHIMMER_ENTITY_CUTOUT = Util.memoize(texture -> create("shimmer:entity_cutout", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, false, CompositeState.builder()
		.setShaderState(RENDERTYPE_ENTITY_CUTOUT_SHADER)
		.setTextureState(new AutoTextureStateShard(texture))
		.setTransparencyState(NO_TRANSPARENCY)
		.setLightmapState(LIGHTMAP)
		.setOverlayState(OVERLAY)
		.createCompositeState(true))
	);

	public static final Function<ResourceLocation, RenderType> SHIMMER_ENTITY_TRANSLUCENT_CULL = Util.memoize(texture -> create("shimmer:entity_translucent_cull", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, true, CompositeState.builder()
		.setShaderState(RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL_SHADER)
		.setTextureState(new AutoTextureStateShard(texture))
		.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
		.setLightmapState(LIGHTMAP)
		.setOverlayState(OVERLAY)
		.createCompositeState(true))
	);

	public static final Function<ResourceLocation, RenderType> SKYBOX = Util.memoize(texture -> create("shimmer:skybox", DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 1536, true, true, CompositeState.builder()
		.setShaderState(POSITION_TEXTURE_COLOR_SHADER)
		.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, false))
		.createCompositeState(true))
	);

	private ShimmerRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
		super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
	}

	public static RenderType entityTextureCull(ResourceLocation texture, boolean translucent) {
		return translucent ? SHIMMER_ENTITY_TRANSLUCENT_CULL.apply(texture) : SHIMMER_ENTITY_CUTOUT.apply(texture);
	}
}
