package dev.latvian.mods.vidlib.feature.bloom;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.canvas.Canvas;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;

public class Bloom {
	@AutoRegister(Dist.CLIENT)
	public static final Canvas CANVAS = Canvas.createExternal(VidLib.id("bloom"));

	public static RenderType pos(ResourceLocation texture) {
		return BloomRenderTypes.POS.apply(texture);
	}

	public static VertexConsumer posBuffer(MultiBufferSource buffers, ResourceLocation texture) {
		return buffers.getBuffer(pos(texture));
	}

	public static RenderType posCol(ResourceLocation texture) {
		return BloomRenderTypes.POS_COL.apply(texture);
	}

	public static VertexConsumer posColBuffer(MultiBufferSource buffers, ResourceLocation texture) {
		return buffers.getBuffer(posCol(texture));
	}

	public static RenderType posTexCol(ResourceLocation texture) {
		return BloomRenderTypes.POS_TEX_COL.apply(texture);
	}

	public static VertexConsumer posTexColBuffer(MultiBufferSource buffers, ResourceLocation texture) {
		return buffers.getBuffer(posTexCol(texture));
	}
}
