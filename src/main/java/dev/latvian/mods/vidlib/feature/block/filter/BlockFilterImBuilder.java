package dev.latvian.mods.vidlib.feature.block.filter;

import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import org.jetbrains.annotations.Nullable;

public interface BlockFilterImBuilder extends ImBuilder<BlockFilter> {
	ImBuilderWrapper.Factory<BlockFilter> IMGUI_BUILDER_FACTORY = new ImBuilderWrapper.Factory<>(BlockFilterImBuilderEvent::new);

	static ImBuilderWrapper<BlockFilter> create(@Nullable BlockFilter defaultFilter) {
		var builder = new ImBuilderWrapper<>(IMGUI_BUILDER_FACTORY);

		if (defaultFilter != null) {
			builder.selectUnit(defaultFilter);
		}

		return builder;
	}

	static ImBuilderWrapper<BlockFilter> create() {
		return create(BlockFilter.NONE.instance());
	}
}
