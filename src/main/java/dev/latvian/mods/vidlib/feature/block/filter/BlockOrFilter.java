package dev.latvian.mods.vidlib.feature.block.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
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

public record BlockOrFilter(List<BlockFilter> filters) implements BlockFilter, ImBuilderWrapper.BuilderSupplier {
	public static SimpleRegistryType<BlockOrFilter> TYPE = SimpleRegistryType.dynamic("or", RecordCodecBuilder.mapCodec(instance -> instance.group(
		BlockFilter.CODEC.listOf().fieldOf("filters").forGetter(BlockOrFilter::filters)
	).apply(instance, BlockOrFilter::new)), KLibStreamCodecs.listOf(BlockFilter.STREAM_CODEC).map(BlockOrFilter::new, BlockOrFilter::filters));

	public static class Builder implements BlockFilterImBuilder {
		public static final ImBuilderHolder<BlockFilter> TYPE = new ImBuilderHolder<>("OR", Builder::new);

		public final List<ImBuilderWrapper<BlockFilter>> filters;

		public Builder() {
			this.filters = new ArrayList<>(2);
			this.filters.add(BlockFilterImBuilder.create());
			this.filters.add(BlockFilterImBuilder.create());
		}

		@Override
		public void set(BlockFilter value) {
			if (value instanceof BlockOrFilter f) {
				filters.clear();

				for (var filter : f.filters) {
					var filterBuilder = BlockFilterImBuilder.create();
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

			return new BlockOrFilter(list);
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(BlockInWorld block) {
		for (var filter : filters) {
			if (filter.test(block)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean test(Level level, BlockPos pos, BlockState state) {
		for (var filter : filters) {
			if (filter.test(level, pos, state)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public BlockFilter or(BlockFilter filter) {
		if (filter == ANY.instance()) {
			return filter;
		} else if (filter == NONE.instance()) {
			return this;
		}

		var list = new ArrayList<>(filters);
		list.add(filter);
		return new BlockOrFilter(List.copyOf(list));
	}

	@Override
	public ImBuilderHolder<?> getImBuilderHolder() {
		return Builder.TYPE;
	}
}
