package dev.latvian.mods.vidlib.util.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public record MultiBufferSourceOverride(MultiBufferSource delegate, Function<ResourceLocation, RenderType> cull, Function<ResourceLocation, RenderType> noCull) implements MultiBufferSource {
	public MultiBufferSourceOverride(MultiBufferSource delegate, Function<ResourceLocation, RenderType> override) {
		this(delegate, override, override);
	}

	@Override
	public VertexConsumer getBuffer(RenderType renderType) {
		var tex = renderType.vl$getTextureSafe();

		if (renderType.getRenderPipeline().isCull()) {
			return delegate.getBuffer(cull.apply(tex));
		} else {
			return delegate.getBuffer(noCull.apply(tex));
		}
	}
}
