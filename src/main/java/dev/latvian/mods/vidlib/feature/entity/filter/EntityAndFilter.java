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

public record EntityAndFilter(List<EntityFilter> filters) implements EntityFilter, ImBuilderWrapper.BuilderSupplier {
	public static SimpleRegistryType<EntityAndFilter> TYPE = SimpleRegistryType.dynamic("and", RecordCodecBuilder.mapCodec(instance -> instance.group(
		EntityFilter.CODEC.listOf().fieldOf("filters").forGetter(EntityAndFilter::filters)
	).apply(instance, EntityAndFilter::new)), KLibStreamCodecs.listOf(EntityFilter.STREAM_CODEC).map(EntityAndFilter::new, EntityAndFilter::filters));

	public static class Builder implements EntityFilterImBuilder {
		public static final ImBuilderHolder<EntityFilter> TYPE = new ImBuilderHolder<>("AND", Builder::new);

		public final List<ImBuilderWrapper<EntityFilter>> filters;

		public Builder() {
			this.filters = new ArrayList<>(2);
			this.filters.add(EntityFilterImBuilder.create());
			this.filters.add(EntityFilterImBuilder.create());
		}

		@Override
		public void set(EntityFilter value) {
			if (value instanceof EntityAndFilter f) {
				filters.clear();

				for (var filter : f.filters) {
					var filterBuilder = EntityFilterImBuilder.create();
					filterBuilder.set(filter);
					filters.add(filterBuilder);
				}
			}
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

				ImGui.indent();
				update = update.or(filter.imgui(graphics));
				ImGui.unindent();
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

			return new EntityAndFilter(list);
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(Entity entity) {
		for (var filter : filters) {
			if (!filter.test(entity)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public EntityFilter and(EntityFilter filter) {
		if (filter == ANY.instance()) {
			return this;
		} else if (filter == NONE.instance()) {
			return filter;
		}

		var list = new ArrayList<>(filters);
		list.add(filter);
		return new EntityAndFilter(List.copyOf(list));
	}

	@Override
	public ImBuilderHolder<?> getImBuilderHolder() {
		return Builder.TYPE;
	}
}
