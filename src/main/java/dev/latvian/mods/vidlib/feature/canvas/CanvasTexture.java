package dev.latvian.mods.vidlib.feature.canvas;

import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
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
	public GpuTextureView getTextureView() {
		var t = canvas.getOutputTarget();
		var view = t == null ? null : depth ? t.getDepthTextureView() : t.getColorTextureView();

		if (view == null) {
			throw new IllegalStateException("Canvas " + canvas.idString + (depth ? " depth texture view does not exist" : " color texture view does not exist"));
		} else {
			return view;
		}
	}

	@Override
	public void close() {
	}
}
