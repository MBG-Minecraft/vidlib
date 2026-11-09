package dev.latvian.mods.vidlib.feature.client;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public interface VLRenderTypes {
	TexturedRenderType STONE_ENTITY_CUTOUT = TexturedRenderType.create(
		(texture) -> {
			RenderSetup state = RenderSetup.builder(VLRenderPipelines.STONE_ENTITY_CUTOUT)
				.withTexture("Sampler0", texture)
				.useLightmap()
				.useOverlay()
				.affectsCrumbling()
				.setOutline(RenderSetup.OutlineProperty.NONE)
				.createRenderSetup();
			return RenderType.create("entity_cutout", state);
		}
	);

	TexturedRenderType STONE_ENTITY_CUTOUT_AFFECTING_OUTLINE = TexturedRenderType.create(
		(texture) -> {
			RenderSetup state = RenderSetup.builder(VLRenderPipelines.STONE_ENTITY_CUTOUT)
				.withTexture("Sampler0", texture)
				.useLightmap()
				.useOverlay()
				.affectsCrumbling()
				.setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
				.createRenderSetup();
			return RenderType.create("entity_cutout", state);
		}
	);
}
