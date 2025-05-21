package dev.latvian.mods.vidlib.feature.canvas;

import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import dev.latvian.mods.vidlib.feature.client.GLDebugLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

public class ExternalCanvas extends Canvas implements Consumer<RenderPass> {
	RenderTarget externalTarget;
	public final List<CanvasPassModifier> passModifiers;

	protected ExternalCanvas(ResourceLocation id) {
		super(id);
		this.passModifiers = new ArrayList<>(0);
	}

	public <T extends CanvasPassModifier> T modifier(T modifier) {
		passModifiers.add(modifier);
		return modifier;
	}

	public CanvasSampler sampler(String name) {
		return modifier(new CanvasSampler(name));
	}

	public CanvasIntUniform intUniform(String name) {
		return modifier(new CanvasIntUniform(name, UniformType.INT));
	}

	public CanvasIntUniform ivec3Uniform(String name) {
		return modifier(new CanvasIntUniform(name, UniformType.IVEC3));
	}

	public CanvasFloatUniform floatUniform(String name) {
		return modifier(new CanvasFloatUniform(name, UniformType.FLOAT));
	}

	public CanvasFloatUniform vec2Uniform(String name) {
		return modifier(new CanvasFloatUniform(name, UniformType.VEC2));
	}

	public CanvasFloatUniform vec3Uniform(String name) {
		return modifier(new CanvasFloatUniform(name, UniformType.VEC3));
	}

	public CanvasFloatUniform vec4Uniform(String name) {
		return modifier(new CanvasFloatUniform(name, UniformType.VEC4));
	}

	public CanvasFloatUniform mat4Uniform(String name) {
		return modifier(new CanvasFloatUniform(name, UniformType.MATRIX4X4));
	}

	public void init(int w, int h) {
		close();

		if (data.scale() != 1F) {
			w = Mth.ceil(w * data.scale());
			h = Mth.ceil(h * data.scale());
		}

		externalTarget = new TextureTarget(pathString, w, h, data.depth(), data.stencil());
	}

	public void resize(int w, int h) {
		if (externalTarget != null) {
			if (data.scale() != 1F) {
				w = Mth.ceil(w * data.scale());
				h = Mth.ceil(h * data.scale());
			}

			externalTarget.resize(w, h);
		}
	}

	public void close() {
		if (externalTarget != null) {
			externalTarget.destroyBuffers();
			externalTarget = null;
		}
	}

	public void addToFrame(Minecraft mc, FrameGraphBuilder frameGraphBuilder, PostChain.TargetBundle targetBundle, int w, int h) {
		var targets = defaultTargets;

		if (!data.importTargets().isEmpty()) {
			targets = new HashSet<>(defaultTargets.size() + data.importTargets().size());
			targets.addAll(defaultTargets);
			targets.addAll(data.importTargets());
		}

		var chain = mc.getShaderManager().getPostChain(id, targets);

		if (chain != null) {
			chain.addToFrame(frameGraphBuilder, w, h, targetBundle, this);
		}
	}

	@Override
	public void accept(RenderPass pass) {
		for (var passModifier : passModifiers) {
			passModifier.apply(pass);
		}
	}

	@Override
	@Nullable
	public RenderTarget getOutputTarget() {
		return outputTarget != null ? outputTarget.get() : externalTarget;
	}

	@Override
	public void createHandle(FrameGraphBuilder builder, RenderTargetDescriptor targetDescriptor) {
		GLDebugLog.message("Created external canvas");
		outputTarget = builder.importExternal(pathString, externalTarget);
	}
}
