package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.klib.render.BufferSupplier;
import dev.latvian.mods.klib.util.Empty;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public interface EntityRenderTypes {
	RenderType WHITE = RenderTypes.entitySolid(Empty.TEXTURE);
	RenderType WHITE_TRANSLUCENT = RenderTypes.entityTranslucentCullItemTarget(Empty.TEXTURE);
	RenderType WHITE_TRANSLUCENT_NO_CULL = RenderTypes.entityTranslucent(Empty.TEXTURE);

	TexturedRenderType CUTOUT = entity("entity/cutout", RenderPipelines.ENTITY_CUTOUT_CULL, null, false);
	TexturedRenderType CUTOUT_NO_CULL = entity("entity/cutout_no_cull", RenderPipelines.ENTITY_CUTOUT, null, false);

	BufferSupplier WHITE_CUTOUT_BUFFER_SUPPLIER = BufferSupplier.fixed(CUTOUT.apply(Empty.TEXTURE), CUTOUT_NO_CULL.apply(Empty.TEXTURE));

	TexturedRenderType TRANSLUCENT = entity("entity/translucent", RenderPipelines.ENTITY_TRANSLUCENT_CULL, OutputTarget.ITEM_ENTITY_TARGET, true);
	TexturedRenderType TRANSLUCENT_NO_CULL = entity("entity/translucent_no_cull", RenderPipelines.ENTITY_TRANSLUCENT, null, true);

	BufferSupplier WHITE_TRANSLUCENT_BUFFER_SUPPLIER = BufferSupplier.fixed(TRANSLUCENT.apply(Empty.TEXTURE), TRANSLUCENT_NO_CULL.apply(Empty.TEXTURE));

	TexturedRenderType STONE_CUTOUT_NO_CULL = entity("entity/stone_cutout_no_cull", VidLibRenderPipelines.STONE_ENTITY_NO_CULL, null, false);

	static RenderType textureCull(Identifier texture, boolean translucent) {
		return translucent ? TRANSLUCENT.apply(texture) : CUTOUT.apply(texture);
	}

	static RenderType texture(Identifier texture, boolean translucent) {
		return translucent ? TRANSLUCENT_NO_CULL.apply(texture) : CUTOUT_NO_CULL.apply(texture);
	}

	private static TexturedRenderType entity(String name, com.mojang.blaze3d.pipeline.RenderPipeline pipeline, OutputTarget outputTarget, boolean sortOnUpload) {
		return TexturedRenderType.internal(
			name,
			1536,
			true,
			sortOnUpload,
			texture -> {
				var builder = TexturedRenderType.textured(pipeline, texture, true, true)
					.setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE);

				if (outputTarget != null) {
					builder.setOutputTarget(outputTarget);
				}

				return builder;
			}
		);
	}
}
