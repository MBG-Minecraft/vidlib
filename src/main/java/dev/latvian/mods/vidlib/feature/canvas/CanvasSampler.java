package dev.latvian.mods.vidlib.feature.canvas;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;

import java.util.function.Supplier;

public class CanvasSampler implements CanvasPassModifier {
	public final String name;
	private final Supplier<GpuTexture> valueSupplier;
	private GpuTexture currentTexture;
	private GpuTextureView currentView;

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
			if (value != currentTexture || currentView == null || currentView.isClosed()) {
				if (currentView != null && !currentView.isClosed()) {
					currentView.close();
				}

				currentTexture = value;
				currentView = RenderSystem.getDevice().createTextureView(value);
			}

			pass.bindTexture(name, currentView, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
		}
	}
}
