package dev.latvian.mods.vidlib.feature.canvas;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;

public abstract class CanvasUniform implements CanvasPassModifier {
	public final String name;
	public final UniformType type;

	public CanvasUniform(String name, UniformType type) {
		this.name = name;
		this.type = type;
	}

	@Override
	public void build(RenderPipeline.Builder builder) {
		builder.withUniform(name, type);
	}

	@Override
	public String toString() {
		return name + "[" + type.getSerializedName() + "]";
	}
}
