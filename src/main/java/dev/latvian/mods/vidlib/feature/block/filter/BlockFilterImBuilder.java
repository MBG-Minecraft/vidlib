package dev.latvian.mods.vidlib.feature.block.filter;

import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderType;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import org.jetbrains.annotations.Nullable;

public interface BlockFilterImBuilder extends ImBuilderWithHolder<BlockFilter> {
	ImBuilderWrapper.Factory<BlockFilter> IMGUI_BUILDER_FACTORY = new ImBuilderWrapper.Factory<>(BlockFilterImBuilderEvent::new);
	ImBuilderType<BlockFilter> TYPE = BlockFilterImBuilder::create;

	static ImBuilder<BlockFilter> create(@Nullable BlockFilter defaultValue) {
		var builder = IMGUI_BUILDER_FACTORY.get();

		if (defaultValue != null) {
			builder.set(defaultValue);
		}

		return builder;
	}

	static ImBuilder<BlockFilter> create() {
		return create(BlockFilter.NONE.instance());
	}
}
