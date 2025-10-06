package dev.latvian.mods.vidlib.feature.canvas;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.GpuTexture;

import java.util.function.Supplier;

public class CanvasSampler implements CanvasPassModifier {
	public final String name;
	private final Supplier<GpuTexture> valueSupplier;

	public CanvasSampler(String name, Supplier<GpuTexture> valueSupplier) {
		this.name = name;
		this.valueSupplier = valueSupplier;
	}

	@Override
	public void build(RenderPipeline.Builder builder) {
		builder.withSampler(name);
	}

	@Override
	public void apply(RenderPass pass) {
		var value = valueSupplier.get();

		if (value != null && !value.isClosed()) {
			pass.bindSampler(name, value);
		}
	}
}