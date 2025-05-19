package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
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
}
