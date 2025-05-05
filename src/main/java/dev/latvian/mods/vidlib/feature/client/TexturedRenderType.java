package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public record TexturedRenderType(Map<ResourceLocation, RenderType> map, Function<ResourceLocation, RenderType> factory) implements Function<ResourceLocation, RenderType> {
	public static TexturedRenderType create(Function<ResourceLocation, RenderType> function) {
		return new TexturedRenderType(new ConcurrentHashMap<>(), function);
	}

	public static TexturedRenderType internal(String name, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, RenderPipeline renderPipeline, Function<ResourceLocation, RenderType.CompositeState> state) {
		return create(texture -> RenderType.create(VidLib.id(name).toString(),
			bufferSize,
			affectsCrumbling,
			sortOnUpload,
			renderPipeline,
			state.apply(texture)
		));
	}

	public static TexturedRenderType internal(String name, int bufferSize, RenderPipeline renderPipeline, Function<ResourceLocation, RenderType.CompositeState> state) {
		return internal(name, bufferSize, false, false, renderPipeline, state);
	}

	@Override
	public RenderType apply(ResourceLocation resourceLocation) {
		return map.computeIfAbsent(resourceLocation, factory);
	}
}
