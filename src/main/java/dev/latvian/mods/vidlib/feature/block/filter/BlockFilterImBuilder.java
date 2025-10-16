package dev.latvian.mods.vidlib.feature.block.filter;

import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import org.jetbrains.annotations.Nullable;

public interface BlockFilterImBuilder extends ImBuilderWithHolder<BlockFilter> {
	ImBuilderWrapper.Factory<BlockFilter> IMGUI_BUILDER_FACTORY = new ImBuilderWrapper.Factory<>(BlockFilterImBuilderEvent::new);

	static ImBuilder<BlockFilter> create(@Nullable BlockFilter defaultFilter) {
		return IMGUI_BUILDER_FACTORY.create(defaultFilter);
	}

	static ImBuilder<BlockFilter> create() {
		return create(BlockFilter.NONE.instance());
	}
}
