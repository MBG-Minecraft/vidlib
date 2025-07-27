package dev.latvian.mods.vidlib.feature.entity.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.ParsedEntitySelector;
import dev.latvian.mods.vidlib.feature.entity.ParsedEntitySelectorImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.world.entity.Entity;

public record MatchEntityFilter(ParsedEntitySelector selector) implements EntityFilter, ImBuilderWrapper.BuilderSupplier {
	public static SimpleRegistryType<MatchEntityFilter> TYPE = SimpleRegistryType.dynamic("match", RecordCodecBuilder.mapCodec(instance -> instance.group(
		ParsedEntitySelector.CODEC.fieldOf("selector").forGetter(MatchEntityFilter::selector)
	).apply(instance, MatchEntityFilter::new)), ParsedEntitySelector.STREAM_CODEC.map(MatchEntityFilter::new, MatchEntityFilter::selector));

	public static final Codec<EntityFilter> OPTIONAL_MATCH_CODEC = Codec.STRING.flatXmap(input -> {
		if (input.startsWith("@")) {
			return ParsedEntitySelector.CODEC.parse(JavaOps.INSTANCE, input).map(MatchEntityFilter::new);
		} else {
			return DataResult.error(() -> "String does not start with '@'");
		}
	}, filter -> {
		if (filter instanceof MatchEntityFilter(ParsedEntitySelector s)) {
			return DataResult.success(s.getInput());
		} else {
			return DataResult.error(() -> "Filter is not a MatchEntityFilter");
		}
	});

	public static class Builder implements EntityFilterImBuilder {
		public static final ImBuilderHolder<EntityFilter> TYPE = new ImBuilderHolder<>("Match Selector", Builder::new);

		public final ParsedEntitySelectorImBuilder selector = new ParsedEntitySelectorImBuilder();

		@Override
		public void set(EntityFilter value) {
			if (value instanceof MatchEntityFilter f) {
				selector.set(f.selector);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			return selector.imgui(graphics);
		}

		@Override
		public boolean isValid() {
			return selector.isValid();
		}

		@Override
		public EntityFilter build() {
			return new MatchEntityFilter(selector.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(Entity entity) {
		var s = selector.getSelector();
		return s != null && s.test(entity);
	}

	@Override
	public ImBuilderHolder<?> getImBuilderHolder() {
		return Builder.TYPE;
	}
}
