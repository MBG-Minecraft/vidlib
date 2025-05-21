package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.latvian.mods.kmath.render.vertexconsumer.DelegateVertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;

public record BrightVertexConsumer(VertexConsumer delegate) implements DelegateVertexConsumer {
	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		delegate.addVertex(x, y, z).setNormal(0F, 1F, 0F).setLight(LightTexture.FULL_BRIGHT).setOverlay(OverlayTexture.NO_OVERLAY);
		return this;
	}

	@Override
	public VertexConsumer setColor(int red, int green, int blue, int alpha) {
		delegate.setColor(red, green, blue, alpha);
		return this;
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		delegate.setUv(u, v);
		return this;
	}
}
