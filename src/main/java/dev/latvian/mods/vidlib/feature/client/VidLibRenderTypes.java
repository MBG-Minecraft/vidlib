package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.latvian.mods.vidlib.feature.canvas.Canvas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
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

	TexturedRenderType GUI_BLUR = TexturedRenderType.internal(
		"gui_blur",
		786432,
		RenderPipelines.GUI_TEXTURED,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.TRUE, false))
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

	RenderStateShard.EmptyTextureStateShard THIN_LINE_WIDTH = new RenderStateShard.EmptyTextureStateShard(() -> RenderSystem.lineWidth(Math.max(2.5F, Minecraft.getInstance().getWindow().getWidth() / 1920.0F * 2.5F)), () -> RenderSystem.lineWidth(1F));

	RenderType LINES = RenderType.create(
		"vidlib:lines",
		1536,
		true,
		true,
		RenderPipelines.LINES,
		RenderType.CompositeState.builder()
			.setLineState(RenderStateShard.DEFAULT_LINE)
			.setTextureState(THIN_LINE_WIDTH)
			.setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
			.setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
			.createCompositeState(false)
	);

	TexturedRenderType OUTLINE = TexturedRenderType.create(texture -> RenderType.create(
		"outline",
		1536,
		RenderPipelines.OUTLINE_CULL,
		RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.setOutputState(RenderStateShard.OUTLINE_TARGET)
			.createCompositeState(RenderType.OutlineProperty.IS_OUTLINE)
	));

	TexturedRenderType OUTLINE_NO_CULL = TexturedRenderType.create(texture -> RenderType.create(
		"outline_no_cull",
		1536,
		RenderPipelines.OUTLINE_NO_CULL,
		RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
			.setOutputState(RenderStateShard.OUTLINE_TARGET)
			.createCompositeState(RenderType.OutlineProperty.IS_OUTLINE)
	));

	TexturedRenderType WEAK_OUTLINE = TexturedRenderType.create(texture -> RenderType.create(
			"weak_outline",
			1536,
			RenderPipelines.OUTLINE_CULL,
			RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, false))
				.setOutputState(Canvas.WEAK_OUTLINE.getOutputStateShard())
				.createCompositeState(RenderType.OutlineProperty.IS_OUTLINE)
		)
	);

	TexturedRenderType WEAK_OUTLINE_NO_CULL = TexturedRenderType.create(texture -> RenderType.create(
			"weak_outline",
			1536,
			RenderPipelines.OUTLINE_NO_CULL,
			RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, false))
				.setOutputState(Canvas.WEAK_OUTLINE.getOutputStateShard())
				.createCompositeState(RenderType.OutlineProperty.IS_OUTLINE)
		)
	);

	TexturedRenderType STRONG_OUTLINE = TexturedRenderType.create(texture -> RenderType.create(
		"strong_outline",
			1536,
		RenderPipelines.OUTLINE_CULL,
			RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, false))
				.setOutputState(Canvas.STRONG_OUTLINE.getOutputStateShard())
				.createCompositeState(RenderType.OutlineProperty.IS_OUTLINE)
		)
	);

	TexturedRenderType STRONG_OUTLINE_NO_CULL = TexturedRenderType.create(texture -> RenderType.create(
			"strong_outline_no_cull",
			1536,
			RenderPipelines.OUTLINE_NO_CULL,
			RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, false))
				.setOutputState(Canvas.STRONG_OUTLINE.getOutputStateShard())
				.createCompositeState(RenderType.OutlineProperty.IS_OUTLINE)
		)
	);
}
