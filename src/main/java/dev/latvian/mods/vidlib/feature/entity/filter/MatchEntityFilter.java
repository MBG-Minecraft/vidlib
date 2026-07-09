package dev.latvian.mods.vidlib.feature.entity.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.util.ParsedEntitySelector;
import dev.latvian.mods.vidlib.feature.entity.ParsedEntitySelectorImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import dev.latvian.mods.vidlib.feature.registry.CustomRegistryType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;

public record MatchEntityFilter(ParsedEntitySelector selector) implements EntityFilter, ImBuilderWithHolder.Factory {
	public static DynamicType<RegistryFriendlyByteBuf, EntityFilter> TYPE = CustomRegistryType.dynamic("match", RecordCodecBuilder.mapCodec(instance -> instance.group(
		ParsedEntitySelector.CODEC.fieldOf("selector").forGetter(MatchEntityFilter::selector)
	).apply(instance, MatchEntityFilter::new)), ParsedEntitySelector.STREAM_CODEC.map(MatchEntityFilter::new, MatchEntityFilter::selector));

	public static class Builder implements EntityFilterImBuilder {
		public static final ImBuilderHolder<EntityFilter> TYPE = ImBuilderHolder.of("Match Selector", Builder::new);

		public final ParsedEntitySelectorImBuilder selector = new ParsedEntitySelectorImBuilder();

		@Override
		public ImBuilderHolder<?> holder() {
			return TYPE;
		}

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
	public DynamicType<RegistryFriendlyByteBuf, EntityFilter> type() {
		return TYPE;
	}

	@Override
	public boolean test(Entity entity) {
		var s = selector.getSelector();
		return s != null && s.test(entity);
	}

	@Override
	public ImBuilderWithHolder<?> createImBuilder() {
		return new Builder();
	}
}
