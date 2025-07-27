package dev.latvian.mods.vidlib.feature.entity.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import dev.latvian.mods.vidlib.feature.imgui.builder.TagKeyImBuilder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public record EntityTypeTagFilter(TagKey<EntityType<?>> tag) implements EntityFilter, ImBuilderWrapper.BuilderSupplier {
	public static SimpleRegistryType<EntityTypeTagFilter> TYPE = SimpleRegistryType.dynamic("type_tag", RecordCodecBuilder.mapCodec(instance -> instance.group(
		TagKey.codec(Registries.ENTITY_TYPE).fieldOf("tag").forGetter(EntityTypeTagFilter::tag)
	).apply(instance, EntityTypeTagFilter::new)), ByteBufCodecs.fromCodecWithRegistries(TagKey.codec(Registries.ENTITY_TYPE)).map(EntityTypeTagFilter::new, EntityTypeTagFilter::tag));

	public static class Builder implements EntityFilterImBuilder {
		public static final ImBuilderHolder<EntityFilter> TYPE = new ImBuilderHolder<>("Type Tag", Builder::new);

		public final TagKeyImBuilder<EntityType<?>> tag = new TagKeyImBuilder<>(Registries.ENTITY_TYPE);

		@Override
		public void set(EntityFilter value) {
			if (value instanceof EntityTypeTagFilter f) {
				tag.set(f.tag);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			return tag.imgui(graphics);
		}

		@Override
		public boolean isValid() {
			return tag.isValid();
		}

		@Override
		public EntityFilter build() {
			return new EntityTypeTagFilter(tag.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(Entity entity) {
		return entity.getType().builtInRegistryHolder().is(tag);
	}

	@Override
	public ImBuilderHolder<?> getImBuilderHolder() {
		return Builder.TYPE;
	}
}
