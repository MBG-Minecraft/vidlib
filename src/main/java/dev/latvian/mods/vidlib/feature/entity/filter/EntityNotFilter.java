package dev.latvian.mods.vidlib.feature.entity.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import imgui.ImGui;
import net.minecraft.world.entity.Entity;

public record EntityNotFilter(EntityFilter filter) implements EntityFilter {
	public static SimpleRegistryType<EntityNotFilter> TYPE = SimpleRegistryType.dynamic("not", RecordCodecBuilder.mapCodec(instance -> instance.group(
		EntityFilter.CODEC.fieldOf("filter").forGetter(EntityNotFilter::filter)
	).apply(instance, EntityNotFilter::new)), EntityFilter.STREAM_CODEC.map(EntityNotFilter::new, EntityNotFilter::filter));

	public static class Builder implements EntityFilterImBuilder {
		public static final ImBuilderHolder<EntityFilter> TYPE = new ImBuilderHolder<>("NOT", Builder::new);

		public final ImBuilder<EntityFilter> filter = EntityFilterImBuilder.create();

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			ImGui.pushID("###not");
			var update = filter.imgui(graphics);
			ImGui.popID();
			return update;
		}

		@Override
		public boolean isValid() {
			return filter.isValid();
		}

		@Override
		public EntityFilter build() {
			return filter.build().not();
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(Entity entity) {
		return !filter.test(entity);
	}

	@Override
	public EntityFilter not() {
		return filter;
	}
}
