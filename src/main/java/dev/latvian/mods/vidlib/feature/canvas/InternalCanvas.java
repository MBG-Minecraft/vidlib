package dev.latvian.mods.vidlib.feature.canvas;

import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class InternalCanvas extends Canvas {
	protected InternalCanvas(Identifier id, Consumer<CanvasBuilder> builder) {
		super(id, builder);
	}

	@Override
	@Nullable
	public RenderTarget getOutputTarget() {
		try {
			return outputTarget != null ? outputTarget.get() : null;
		} catch (Exception ex) {
			return null;
		}
	}

	@Override
	public void createHandle(FrameGraphBuilder builder, RenderTargetDescriptor targetDescriptor) {
		outputTarget = builder.createInternal(pathString, targetDescriptor);
	}
}
