package dev.latvian.mods.vidlib.feature.canvas;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;

public interface CanvasPassModifier {
	void build(RenderPipeline.Builder builder);

	void apply(RenderPass pass);
}
