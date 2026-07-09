package dev.latvian.mods.vidlib.feature.block.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.registry.CustomRegistryType;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImGui;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.util.ArrayList;
import java.util.List;

public record BlockAndFilter(List<Ref<BlockFilter>> filters) implements BlockFilter, ImBuilderWithHolder.Factory {
	public static CustomRegistryType<RegistryFriendlyByteBuf, BlockFilter> TYPE = REGISTRY.dynamic(ID.vidlib("and"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		BlockFilter.CODEC.listOf().fieldOf("filters").forGetter(BlockAndFilter::filters)
	).apply(instance, BlockAndFilter::new)), KLibStreamCodecs.listOf(BlockFilter.STREAM_CODEC).map(BlockAndFilter::new, BlockAndFilter::filters));

	public static class Builder implements BlockFilterImBuilder {
		public static final ImBuilderHolder<BlockFilter> TYPE = ImBuilderHolder.of("AND", Builder::new);

		public final List<ImBuilder<BlockFilter>> filters;

		public Builder() {
			this.filters = new ArrayList<>(2);
			this.filters.add(BlockFilterImBuilder.create());
			this.filters.add(BlockFilterImBuilder.create());
		}

		@Override
		public ImBuilderHolder<?> holder() {
			return TYPE;
		}

		@Override
		public void set(BlockFilter value) {
			if (value instanceof BlockAndFilter f) {
				filters.clear();

				for (var filter : f.filters) {
					var filterBuilder = BlockFilterImBuilder.create();
					filterBuilder.set(filter.value());
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

				boolean deleted = ImGui.smallButton(ImIcons.TRASHCAN + " Delete###delete-filter");

				graphics.popStack();

				ImGui.indent();
				update = update.or(filter.imgui(graphics));
				ImGui.unindent();
				ImGui.popID();

				if (deleted) {
					filters.remove(i);
					i--;
					update = ImUpdate.FULL;
				}
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
			var list = new ArrayList<Ref<BlockFilter>>(filters.size());

			for (var filter : filters) {
				list.add(filter.build().ref());
			}

			return new BlockAndFilter(list);
		}
	}

	@Override
	public CustomRegistryType<RegistryFriendlyByteBuf, BlockFilter> type() {
		return TYPE;
	}

	@Override
	public boolean test(BlockInWorld block) {
		for (var filter : filters) {
			if (!filter.value().test(block)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean test(Level level, BlockPos pos, BlockState state) {
		for (var filter : filters) {
			if (!filter.value().test(level, pos, state)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public BlockFilter and(BlockFilter filter) {
		if (filter == BlockFilter.of(true)) {
			return this;
		} else if (filter == BlockFilter.of(false)) {
			return filter;
		}

		var list = new ArrayList<Ref<BlockFilter>>(filters.size() + 1);
		list.addAll(filters);
		list.add(filter.ref());
		return new BlockAndFilter(List.copyOf(list));
	}

	@Override
	public ImBuilderWithHolder<?> createImBuilder() {
		return new Builder();
	}
}
