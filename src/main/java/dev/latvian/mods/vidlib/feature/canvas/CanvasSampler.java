package dev.latvian.mods.vidlib.feature.canvas;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.GpuTexture;

public class CanvasSampler implements CanvasPassModifier {
	public final String name;
	private GpuTexture value;

	public CanvasSampler(String name) {
		this.name = name;
		this.value = null;
	}

	public CanvasSampler set(GpuTexture value) {
		this.value = value;
		return this;
	}

	@Override
	public void build(RenderPipeline.Builder builder) {
		builder.withSampler(name);
	}

	@Override
	public void apply(RenderPass pass) {
		if (value != null && !value.isClosed()) {
			pass.bindSampler(name, value);
		}
	}
}