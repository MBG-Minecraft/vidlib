package dev.latvian.mods.vidlib.feature.block.filter;

import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import org.jetbrains.annotations.Nullable;

public interface BlockFilterImBuilder extends ImBuilder<BlockFilter> {
	static ImBuilderWrapper<BlockFilter> create(@Nullable BlockFilter defaultFilter) {
		var builder = new ImBuilderWrapper<>(BlockFilter.IMGUI_BUILDERS);

		if (defaultFilter != null) {
			builder.selectUnit(defaultFilter);
		}

		return builder;
	}

	static ImBuilderWrapper<BlockFilter> create() {
		return create(BlockFilter.NONE.instance());
	}
}
