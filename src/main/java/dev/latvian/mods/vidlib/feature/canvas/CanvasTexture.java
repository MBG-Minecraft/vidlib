package dev.latvian.mods.vidlib.feature.canvas;

import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.renderer.texture.AbstractTexture;

public class CanvasTexture extends AbstractTexture {
	public final Canvas canvas;
	public final boolean depth;

	public CanvasTexture(Canvas canvas, boolean depth) {
		this.canvas = canvas;
		this.depth = depth;
	}

	@Override
	public GpuTexture getTexture() {
		var t = depth ? canvas.getDepthTexture() : canvas.getColorTexture();

		if (t == null) {
			throw new IllegalStateException("Canvas " + canvas.idString + (depth ? " depth texture does not exist" : " color texture does not exist"));
		} else {
			return t;
		}
	}

	@Override
	public void setClamp(boolean clamp) {
	}

	@Override
	public void setFilter(boolean blur, boolean mipmap) {
	}

	@Override
	public void close() {
	}
}
