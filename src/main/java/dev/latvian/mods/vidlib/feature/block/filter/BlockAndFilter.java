package dev.latvian.mods.vidlib.feature.block.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import imgui.ImGui;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.util.ArrayList;
import java.util.List;

public record BlockAndFilter(List<BlockFilter> filters) implements BlockFilter {
	public static SimpleRegistryType<BlockAndFilter> TYPE = SimpleRegistryType.dynamic("and", RecordCodecBuilder.mapCodec(instance -> instance.group(
		BlockFilter.CODEC.listOf().fieldOf("filters").forGetter(BlockAndFilter::filters)
	).apply(instance, BlockAndFilter::new)), BlockFilter.STREAM_CODEC.listOf().map(BlockAndFilter::new, BlockAndFilter::filters));

	public static class Builder implements BlockFilterImBuilder {
		public static final ImBuilderHolder<BlockFilter> TYPE = new ImBuilderHolder<>("AND", Builder::new);

		public final List<ImBuilderWrapper<BlockFilter>> filters;

		public Builder() {
			this.filters = new ArrayList<>(2);
			this.filters.add(BlockFilterImBuilder.create());
			this.filters.add(BlockFilterImBuilder.create());
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
				filters.add(BlockFilterImBuilder.create());
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
		public BlockFilter build() {
			var list = new ArrayList<BlockFilter>(filters.size());

			for (var filter : filters) {
				list.add(filter.build());
			}

			return new BlockAndFilter(list);
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(BlockInWorld block) {
		for (var filter : filters) {
			if (!filter.test(block)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean test(Level level, BlockPos pos, BlockState state) {
		for (var filter : filters) {
			if (!filter.test(level, pos, state)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public BlockFilter and(BlockFilter filter) {
		if (filter == ANY.instance()) {
			return this;
		} else if (filter == NONE.instance()) {
			return filter;
		}

		var list = new ArrayList<>(filters);
		list.add(filter);
		return new BlockAndFilter(List.copyOf(list));
	}
}
