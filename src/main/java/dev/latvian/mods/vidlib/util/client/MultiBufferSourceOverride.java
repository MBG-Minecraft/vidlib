package dev.latvian.mods.vidlib.util.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.latvian.mods.vidlib.feature.bloom.BloomRenderTypes;
import dev.latvian.mods.vidlib.feature.canvas.BossEntityRenderTypes;
import dev.latvian.mods.vidlib.feature.client.BrightRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public record MultiBufferSourceOverride(MultiBufferSource delegate, Function<RenderType, RenderType> override) implements MultiBufferSource {
	public static MultiBufferSourceOverride boss(MultiBufferSource delegate) {
		return new MultiBufferSourceOverride(delegate, BossEntityRenderTypes.CULL, BossEntityRenderTypes.NO_CULL);
	}

	public static MultiBufferSourceOverride brightPos(MultiBufferSource delegate) {
		return new MultiBufferSourceOverride(delegate, BrightRenderTypes.POS, BrightRenderTypes.POS_NO_CULL);
	}

	public static MultiBufferSourceOverride brightPosCol(MultiBufferSource delegate) {
		return new MultiBufferSourceOverride(delegate, BrightRenderTypes.POS_COL, BrightRenderTypes.POS_COL_NO_CULL);
	}

	public static MultiBufferSourceOverride brightPosTexCol(MultiBufferSource delegate) {
		return new MultiBufferSourceOverride(delegate, BrightRenderTypes.POS_TEX_COL, BrightRenderTypes.POS_TEX_COL_NO_CULL);
	}

	public static MultiBufferSourceOverride bloomPos(MultiBufferSource delegate) {
		return new MultiBufferSourceOverride(delegate, BloomRenderTypes.POS, BloomRenderTypes.POS_NO_CULL);
	}

	public static MultiBufferSourceOverride bloomPosCol(MultiBufferSource delegate) {
		return new MultiBufferSourceOverride(delegate, BloomRenderTypes.POS_COL, BloomRenderTypes.POS_COL_NO_CULL);
	}

	public static MultiBufferSourceOverride bloomPosTexCol(MultiBufferSource delegate) {
		return new MultiBufferSourceOverride(delegate, BloomRenderTypes.POS_TEX_COL, BloomRenderTypes.POS_TEX_COL_NO_CULL);
	}

	public MultiBufferSourceOverride(MultiBufferSource delegate, RenderType override) {
		this(delegate, type -> override);
	}

	public MultiBufferSourceOverride(MultiBufferSource delegate, Function<ResourceLocation, RenderType> cull, Function<ResourceLocation, RenderType> noCull) {
		this(delegate, type -> (type.getRenderPipeline().isCull() ? cull : noCull).apply(type.vl$getTextureSafe()));
	}

	@Override
	public VertexConsumer getBuffer(RenderType renderType) {
		return delegate.getBuffer(override.apply(renderType));
	}
}
