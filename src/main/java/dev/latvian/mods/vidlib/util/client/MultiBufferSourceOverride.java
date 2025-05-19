package dev.latvian.mods.vidlib.util.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public record MultiBufferSourceOverride(MultiBufferSource delegate, Function<RenderType, RenderType> override) implements MultiBufferSource {
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
