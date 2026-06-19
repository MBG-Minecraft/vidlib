package dev.latvian.mods.vidlib.util.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

import java.util.function.Function;

public record MultiBufferSourceOverride(MultiBufferSource delegate, Function<Identifier, RenderType> cull, Function<Identifier, RenderType> noCull) implements MultiBufferSource {
	public MultiBufferSourceOverride(MultiBufferSource delegate, Function<Identifier, RenderType> override) {
		this(delegate, override, override);
	}

	@Override
	public VertexConsumer getBuffer(RenderType renderType) {
		var tex = renderType.vl$getTextureSafe();

		if (renderType.pipeline().isCull()) {
			return delegate.getBuffer(cull.apply(tex));
		} else {
			return delegate.getBuffer(noCull.apply(tex));
		}
	}
}
