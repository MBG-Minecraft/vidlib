package dev.latvian.mods.vidlib.feature.entity.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.DimensionImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public record InDimensionEntityFilter(ResourceKey<Level> dimension) implements EntityFilter, ImBuilderWithHolder.Factory {
	public static SimpleRegistryType<InDimensionEntityFilter> TYPE = SimpleRegistryType.dynamic("in_dimension", RecordCodecBuilder.mapCodec(instance -> instance.group(
		MCCodecs.DIMENSION.optionalFieldOf("dimension", Level.OVERWORLD).forGetter(InDimensionEntityFilter::dimension)
	).apply(instance, InDimensionEntityFilter::new)), KLibStreamCodecs.optional(MCStreamCodecs.DIMENSION, Level.OVERWORLD).map(InDimensionEntityFilter::new, InDimensionEntityFilter::dimension));

	public static class Builder implements EntityFilterImBuilder {
		public static final ImBuilderHolder<EntityFilter> TYPE = ImBuilderHolder.of("In Dimension", Builder::new);

		public final DimensionImBuilder dimension = new DimensionImBuilder();

		@Override
		public ImBuilderHolder<?> holder() {
			return TYPE;
		}

		@Override
		public void set(EntityFilter value) {
			if (value instanceof InDimensionEntityFilter f) {
				dimension.set(f.dimension);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			return dimension.imgui(graphics);
		}

		@Override
		public boolean isValid() {
			return dimension.isValid();
		}

		@Override
		public EntityFilter build() {
			return new InDimensionEntityFilter(dimension.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(Entity entity) {
		return entity.level().dimension() == dimension;
	}

	@Override
	public ImBuilderWithHolder<?> createImBuilder() {
		return new Builder();
	}
}
