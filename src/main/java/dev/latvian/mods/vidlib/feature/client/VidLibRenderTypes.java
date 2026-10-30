package dev.latvian.mods.vidlib.feature.client;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public interface VidLibRenderTypes {
	TexturedRenderType GUI = TexturedRenderType.internal("gui", RenderPipelines.GUI_TEXTURED,
		(pipeline, texture) -> RenderSetup.builder(pipeline)
			.withTexture("Sampler0", texture)
			.createRenderSetup()
	);

	TexturedRenderType GUI_BLUR = TexturedRenderType.internal("gui_blur", RenderPipelines.GUI_TEXTURED,
		(pipeline, texture) -> RenderSetup.builder(pipeline)
			.withTexture("Sampler0", texture)
			.createRenderSetup()
	);

	TexturedRenderType GUI_DEPTH = TexturedRenderType.internal("gui_depth", VidLibRenderPipelines.GUI_DEPTH,
		(pipeline, texture) -> RenderSetup.builder(pipeline)
			.withTexture("Sampler0", texture)
			.createRenderSetup()
	);

	/**
	 * Masked GUI draw. The mask texture is still supplied through the legacy
	 * Sampler1 slot, which Vanilla binds to the overlay texture.
	 */
	Function<Identifier, TexturedRenderType> MASKED_GUI = Util.memoize(maskTexture -> TexturedRenderType.internal("masked_gui", VidLibRenderPipelines.MASKED_GUI,
		(pipeline, texture) -> RenderSetup.builder(pipeline)
			.withTexture("Sampler0", texture)
			.createRenderSetup()
	));

	TexturedRenderType MSDF = TexturedRenderType.internal("msdf", VidLibRenderPipelines.MSDF,
		(pipeline, texture) -> RenderSetup.builder(pipeline)
			.withTexture("Sampler0", texture)
			.createRenderSetup()
	);

	TexturedRenderType MSDF_SEE_THROUGH = TexturedRenderType.internal("msdf_see_through", VidLibRenderPipelines.MSDF_SEE_THROUGH,
		(pipeline, texture) -> RenderSetup.builder(pipeline)
			.withTexture("Sampler0", texture)
			.createRenderSetup()
	);

	TexturedRenderType SKYBOX = TexturedRenderType.internal("skybox", VidLibRenderPipelines.SKYBOX,
		(pipeline, texture) -> RenderSetup.builder(pipeline)
			.withTexture("Sampler0", texture)
			.createRenderSetup()
	);

	/**
	 * Configurable-width lines. 26.3 exposes line width through the shader uniform
	 * writing the line width itself.
	 */
	RenderType LINES = RenderType.create("vidlib:lines", RenderSetup.builder(RenderPipelines.LINES)
		.createRenderSetup());

	TexturedRenderType OUTLINE = TexturedRenderType.create(texture -> RenderType.create("vidlib:outline",
		RenderSetup.builder(RenderPipelines.OUTLINE_CULL)
			.withTexture("Sampler0", texture)
			.setOutline(RenderSetup.OutlineProperty.IS_OUTLINE)
			.createRenderSetup()
	));

	TexturedRenderType OUTLINE_NO_CULL = TexturedRenderType.create(texture -> RenderType.create("vidlib:outline_no_cull",
		RenderSetup.builder(RenderPipelines.OUTLINE_NO_CULL)
			.withTexture("Sampler0", texture)
			.setOutline(RenderSetup.OutlineProperty.IS_OUTLINE)
			.createRenderSetup()
	));
}
