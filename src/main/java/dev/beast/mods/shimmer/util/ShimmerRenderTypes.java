package dev.beast.mods.shimmer.util;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import org.lwjgl.opengl.GL11;

public class ShimmerRenderTypes extends RenderType {
	public static final RenderType WHITE_ENTITY = entitySolid(Empty.TEXTURE);
	public static final RenderType WHITE_TRANSLUCENT_ENTITY = entityTranslucentCull(Empty.TEXTURE);
	public static final RenderType WHITE_TRANSLUCENT_NO_CULL_ENTITY = entityTranslucent(Empty.TEXTURE);

	public static double lineWidth(Minecraft mc) {
		return Math.max(2.5D, (float) mc.getWindow().getWidth() / 1920.0D * 2.5D);
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
			// .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
			.setTextureState(new EmptyTextureStateShard(ShimmerRenderTypes::setupLines, ShimmerRenderTypes::clearLines))
			.setTransparencyState(NO_TRANSPARENCY)
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

	private ShimmerRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
		super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
	}
}
