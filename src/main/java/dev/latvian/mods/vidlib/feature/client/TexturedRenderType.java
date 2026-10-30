package dev.latvian.mods.vidlib.feature.client;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import dev.latvian.mods.klib.util.ID;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Cache of per-texture {@link RenderType}s.
 * <p>
 * 26.3 changed {@code RenderType} from a builder-based {@code CompositeState} to
 * {@code RenderType.create(name, RenderSetup)}, where the pipeline (and therefore the
 * vertex format / blend / depth state) is fixed up front and only the texture binding varies.
 * The factory below receives the texture identifier and returns a fully built {@link RenderType}.
 */
public record TexturedRenderType(Map<Identifier, RenderType> map, Function<Identifier, RenderType> factory, RenderPipeline pipeline) implements Function<Identifier, RenderType> {
	private static final Map<RenderType, Identifier> REVERSE = new IdentityHashMap<>();

	public static TexturedRenderType create(Function<Identifier, RenderType> function) {
		return new TexturedRenderType(new ConcurrentHashMap<>(), function, null);
	}

	/**
	 * @param name           vidlib-namespaced base name of the render type
	 * @param renderPipeline pipeline to use for every texture variant
	 * @param state          builds the {@link RenderSetup} for a given texture
	 */
	public static TexturedRenderType internal(String name, RenderPipeline renderPipeline, Function<Identifier, RenderSetup> state) {
		return create(renderPipeline, texture -> RenderType.create(ID.vidlib(name).toString(), state.apply(texture)));
	}

	/**
	 * Same as {@link #internal(String, RenderPipeline, Function)} but hands the pipeline to the
	 * factory so call sites can build their {@link RenderSetup} without repeating it.
	 */
	public static TexturedRenderType internal(String name, RenderPipeline renderPipeline, BiFunction<RenderPipeline, Identifier, RenderSetup> state) {
		return internal(name, renderPipeline, texture -> state.apply(renderPipeline, texture));
	}

	public static TexturedRenderType video(String name, RenderPipeline renderPipeline, Function<Identifier, RenderSetup> state) {
		return create(renderPipeline, texture -> RenderType.create(ID.video(name).toString(), state.apply(texture)));
	}

	public static TexturedRenderType video(String name, RenderPipeline renderPipeline, BiFunction<RenderPipeline, Identifier, RenderSetup> state) {
		return video(name, renderPipeline, texture -> state.apply(renderPipeline, texture));
	}

	public static TexturedRenderType create(RenderPipeline renderPipeline, Function<Identifier, RenderType> function) {
		return new TexturedRenderType(new ConcurrentHashMap<>(), function, renderPipeline);
	}

	@Override
	public RenderType apply(Identifier resourceLocation) {
		RenderType type = map.computeIfAbsent(resourceLocation, factory);
		REVERSE.put(type, resourceLocation);
		return type;
	}

	public static @Nullable Identifier identifierOf(RenderType renderType) {
		return REVERSE.get(renderType);
	}

	public RenderType apply(ClientAsset asset) {
		return apply(asset.id());
	}

	public void endBatches(SubmitNodeCollector buffers) {
		for (var renderType : map.values()) {
			// Render types are no longer batched explicitly; submitting is stateless in 26.3.
		}
	}
}
