package dev.latvian.mods.vidlib.feature.entity.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import imgui.ImGui;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public record EntityOrFilter(List<EntityFilter> filters) implements EntityFilter {
	public static SimpleRegistryType<EntityOrFilter> TYPE = SimpleRegistryType.dynamic("or", RecordCodecBuilder.mapCodec(instance -> instance.group(
		EntityFilter.CODEC.listOf().fieldOf("filters").forGetter(EntityOrFilter::filters)
	).apply(instance, EntityOrFilter::new)), KLibStreamCodecs.listOf(EntityFilter.STREAM_CODEC).map(EntityOrFilter::new, EntityOrFilter::filters));

	public static class Builder implements EntityFilterImBuilder {
		public static final ImBuilderHolder<EntityFilter> TYPE = new ImBuilderHolder<>("OR", Builder::new);

		public final List<ImBuilderWrapper<EntityFilter>> filters;

		public Builder() {
			this.filters = new ArrayList<>(2);
			this.filters.add(EntityFilterImBuilder.create());
			this.filters.add(EntityFilterImBuilder.create());
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;

			for (int i = 0; i < filters.size(); i++) {
				var filter = filters.get(i);

				ImGui.pushID(i);
				ImGui.text("Filter #" + (i + 1));
				ImGui.sameLine();

				graphics.pushStack();
				graphics.setRedButton();

				boolean deleted = ImGui.smallButton(ImIcons.DELETE + " Delete###delete-filter");

				graphics.popStack();

				update = update.or(filter.imgui(graphics));
				ImGui.popID();

				if (deleted) {
					filter.deleted = true;
				}
			}

			if (filters.removeIf(filter -> filter.deleted)) {
				update = ImUpdate.FULL;
			}

			if (ImGui.smallButton(ImIcons.ADD + " Add")) {
				filters.add(EntityFilterImBuilder.create());
			}

			return update;
		}

		@Override
		public boolean isValid() {
			if (filters.isEmpty()) {
				return false;
			}

			for (var filter : filters) {
				if (!filter.isValid()) {
					return false;
				}
			}

			return true;
		}

		@Override
		public EntityFilter build() {
			var list = new ArrayList<EntityFilter>(filters.size());

			for (var filter : filters) {
				list.add(filter.build());
			}

			return new EntityOrFilter(list);
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(Entity entity) {
		for (var filter : filters) {
			if (filter.test(entity)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public EntityFilter or(EntityFilter filter) {
		if (filter == ANY.instance()) {
			return filter;
		} else if (filter == NONE.instance()) {
			return this;
		}

		var list = new ArrayList<>(filters);
		list.add(filter);
		return new EntityOrFilter(List.copyOf(list));
	}
}
