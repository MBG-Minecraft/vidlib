package dev.latvian.mods.vidlib.feature.entity.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ItemStackImBuilder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

public record HasItemEntityFilter(Ingredient item) implements EntityFilter, ImBuilderWithHolder.Factory {
	public static SimpleRegistryType<HasItemEntityFilter> TYPE = SimpleRegistryType.dynamic("has_item", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Ingredient.CODEC.fieldOf("item").forGetter(HasItemEntityFilter::item)
	).apply(instance, HasItemEntityFilter::new)), CompositeStreamCodec.of(
		Ingredient.CONTENTS_STREAM_CODEC, HasItemEntityFilter::item,
		HasItemEntityFilter::new
	));

	public static class Builder implements EntityFilterImBuilder {
		public static final ImBuilderHolder<EntityFilter> TYPE = ImBuilderHolder.of("Has Item", Builder::new);

		public final ItemStackImBuilder item = new ItemStackImBuilder(false, null);

		@Override
		public ImBuilderHolder<?> holder() {
			return TYPE;
		}

		@Override
		public void set(EntityFilter value) {
			if (value instanceof HasItemEntityFilter f) {
				item.set(f.item.getValues().stream().findFirst().map(Holder::value).map(Item::getDefaultInstance).orElse(null));
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			return item.imguiKey(graphics, "Item", "item");
		}

		@Override
		public boolean isValid() {
			return item.isValid();
		}

		@Override
		public EntityFilter build() {
			return new HasItemEntityFilter(Ingredient.of(item.build().getItem()));
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(Entity entity) {
		return entity.vl$hasItem(item);
	}

	@Override
	public ImBuilderWithHolder<?> createImBuilder() {
		return new Builder();
	}
}
