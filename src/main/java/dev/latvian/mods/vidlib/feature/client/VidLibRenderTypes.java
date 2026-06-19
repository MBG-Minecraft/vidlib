package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.latvian.mods.vidlib.feature.canvas.Canvas;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public interface VidLibRenderTypes {
	TexturedRenderType GUI = TexturedRenderType.internal("gui", 786432, RenderPipelines.GUI_TEXTURED);
	TexturedRenderType GUI_BLUR = TexturedRenderType.internal("gui_blur", 786432, RenderPipelines.GUI_TEXTURED);
	TexturedRenderType GUI_DEPTH = TexturedRenderType.internal("gui_depth", 786432, VidLibRenderPipelines.GUI_DEPTH);

	Function<Identifier, TexturedRenderType> MASKED_GUI = Util.memoize(maskTexture -> TexturedRenderType.internal(
		"masked_gui",
		786432,
		texture -> TexturedRenderType.textured(VidLibRenderPipelines.MASKED_GUI, texture).withTexture("Sampler1", maskTexture)
	));

	TexturedRenderType MSDF = TexturedRenderType.internal("msdf", 786432, VidLibRenderPipelines.MSDF);
	TexturedRenderType MSDF_SEE_THROUGH = TexturedRenderType.internal("msdf_see_through", 786432, VidLibRenderPipelines.MSDF_SEE_THROUGH);
	TexturedRenderType SKYBOX = TexturedRenderType.internal("skybox", DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize() * 6, true, true, VidLibRenderPipelines.SKYBOX);

	RenderType LINES = RenderType.create(
		"vidlib:lines",
		RenderSetup.builder(RenderPipelines.LINES)
			.bufferSize(1536)
			.affectsCrumbling()
			.sortOnUpload()
			.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
			.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
			.createRenderSetup()
	);

	TexturedRenderType OUTLINE = outline("outline", RenderPipelines.OUTLINE_CULL, OutputTarget.OUTLINE_TARGET);
	TexturedRenderType OUTLINE_NO_CULL = outline("outline_no_cull", RenderPipelines.OUTLINE_NO_CULL, OutputTarget.OUTLINE_TARGET);
	TexturedRenderType WEAK_OUTLINE = outline("weak_outline", RenderPipelines.OUTLINE_CULL, Canvas.WEAK_OUTLINE.getOutputTargetBinding());
	TexturedRenderType WEAK_OUTLINE_NO_CULL = outline("weak_outline_no_cull", RenderPipelines.OUTLINE_NO_CULL, Canvas.WEAK_OUTLINE.getOutputTargetBinding());
	TexturedRenderType STRONG_OUTLINE = outline("strong_outline", RenderPipelines.OUTLINE_CULL, Canvas.STRONG_OUTLINE.getOutputTargetBinding());
	TexturedRenderType STRONG_OUTLINE_NO_CULL = outline("strong_outline_no_cull", RenderPipelines.OUTLINE_NO_CULL, Canvas.STRONG_OUTLINE.getOutputTargetBinding());

	private static TexturedRenderType outline(String name, RenderPipeline pipeline, OutputTarget outputTarget) {
		return TexturedRenderType.create(texture -> RenderType.create(
			name,
			RenderSetup.builder(pipeline)
				.withTexture("Sampler0", texture)
				.bufferSize(1536)
				.setOutputTarget(outputTarget)
				.setOutline(RenderSetup.OutlineProperty.IS_OUTLINE)
				.createRenderSetup()
		));
	}
}
