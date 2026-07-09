package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public record TexturedRenderType(Map<Identifier, RenderType> map, Function<Identifier, RenderType> factory) implements Function<Identifier, RenderType> {
	public static final OutputTarget TRANSLUCENT_TARGET = new OutputTarget("translucent_target", () -> Minecraft.getInstance().levelRenderer.getTranslucentTarget());
	public static final OutputTarget PARTICLES_TARGET = new OutputTarget("particles_target", () -> Minecraft.getInstance().levelRenderer.getParticlesTarget());
	public static final OutputTarget CLOUDS_TARGET = new OutputTarget("clouds_target", () -> Minecraft.getInstance().levelRenderer.getCloudsTarget());

	public static TexturedRenderType create(Function<Identifier, RenderType> function) {
		return new TexturedRenderType(new ConcurrentHashMap<>(), function);
	}

	@ApiStatus.Internal
	public static TexturedRenderType internal(String name, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Function<Identifier, RenderSetup.RenderSetupBuilder> state) {
		return create(texture -> RenderType.create(ID.vidlib(name).toString(), createSetup(bufferSize, affectsCrumbling, sortOnUpload, state.apply(texture))));
	}

	@ApiStatus.Internal
	public static TexturedRenderType internal(String name, int bufferSize, Function<Identifier, RenderSetup.RenderSetupBuilder> state) {
		return internal(name, bufferSize, false, false, state);
	}

	@ApiStatus.Internal
	public static TexturedRenderType internal(String name, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, RenderPipeline renderPipeline) {
		return internal(name, bufferSize, affectsCrumbling, sortOnUpload, texture -> textured(renderPipeline, texture));
	}

	@ApiStatus.Internal
	public static TexturedRenderType internal(String name, int bufferSize, RenderPipeline renderPipeline) {
		return internal(name, bufferSize, false, false, renderPipeline);
	}

	public static TexturedRenderType video(String name, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Function<Identifier, RenderSetup.RenderSetupBuilder> state) {
		return create(texture -> RenderType.create(ID.video(name).toString(), createSetup(bufferSize, affectsCrumbling, sortOnUpload, state.apply(texture))));
	}

	public static TexturedRenderType video(String name, int bufferSize, Function<Identifier, RenderSetup.RenderSetupBuilder> state) {
		return video(name, bufferSize, false, false, state);
	}

	public static RenderSetup.RenderSetupBuilder textured(RenderPipeline renderPipeline, Identifier texture) {
		return RenderSetup.builder(renderPipeline).withTexture("Sampler0", texture);
	}

	public static RenderSetup.RenderSetupBuilder textured(RenderPipeline renderPipeline, Identifier texture, boolean lightmap, boolean overlay) {
		var builder = textured(renderPipeline, texture);

		if (lightmap) {
			builder.useLightmap();
		}

		if (overlay) {
			builder.useOverlay();
		}

		return builder;
	}

	public static RenderSetup createSetup(int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, RenderSetup.RenderSetupBuilder builder) {
		builder.bufferSize(bufferSize);

		if (affectsCrumbling) {
			builder.affectsCrumbling();
		}

		if (sortOnUpload) {
			builder.sortOnUpload();
		}

		return builder.createRenderSetup();
	}

	@Override
	public RenderType apply(Identifier Identifier) {
		return map.computeIfAbsent(Identifier, factory);
	}

	public void endBatches(MultiBufferSource.BufferSource buffers) {
		for (var renderType : map.values()) {
			buffers.endBatch(renderType);
		}
	}
}
