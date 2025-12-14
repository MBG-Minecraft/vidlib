package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public record TexturedRenderType(Map<ResourceLocation, RenderType> map, Function<ResourceLocation, RenderType> factory) implements Function<ResourceLocation, RenderType> {
	public static TexturedRenderType create(Function<ResourceLocation, RenderType> function) {
		return new TexturedRenderType(new ConcurrentHashMap<>(), function);
	}

	@ApiStatus.Internal
	public static TexturedRenderType internal(String name, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, RenderPipeline renderPipeline, Function<ResourceLocation, RenderType.CompositeState> state) {
		return create(texture -> RenderType.create(VidLib.id(name).toString(),
			bufferSize,
			affectsCrumbling,
			sortOnUpload,
			renderPipeline,
			state.apply(texture)
		));
	}

	@ApiStatus.Internal
	public static TexturedRenderType internal(String name, int bufferSize, RenderPipeline renderPipeline, Function<ResourceLocation, RenderType.CompositeState> state) {
		return internal(name, bufferSize, false, false, renderPipeline, state);
	}

	public static TexturedRenderType video(String name, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, RenderPipeline renderPipeline, Function<ResourceLocation, RenderType.CompositeState> state) {
		return create(texture -> RenderType.create(ID.video(name).toString(),
			bufferSize,
			affectsCrumbling,
			sortOnUpload,
			renderPipeline,
			state.apply(texture)
		));
	}

	public static TexturedRenderType video(String name, int bufferSize, RenderPipeline renderPipeline, Function<ResourceLocation, RenderType.CompositeState> state) {
		return internal(name, bufferSize, false, false, renderPipeline, state);
	}

	@Override
	public RenderType apply(ResourceLocation resourceLocation) {
		return map.computeIfAbsent(resourceLocation, factory);
	}

	public void endBatches(MultiBufferSource.BufferSource buffers) {
		for (var renderType : map.values()) {
			buffers.endBatch(renderType);
		}
	}
}
