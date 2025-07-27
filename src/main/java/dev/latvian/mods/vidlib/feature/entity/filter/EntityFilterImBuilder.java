package dev.latvian.mods.vidlib.feature.entity.filter;

import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import org.jetbrains.annotations.Nullable;

public interface EntityFilterImBuilder extends ImBuilder<EntityFilter> {
	ImBuilderWrapper.Factory<EntityFilter> IMGUI_BUILDER_FACTORY = new ImBuilderWrapper.Factory<>(EntityFilterImBuilderEvent::new);

	static ImBuilderWrapper<EntityFilter> create(@Nullable EntityFilter defaultFilter) {
		var builder = new ImBuilderWrapper<>(IMGUI_BUILDER_FACTORY);

		if (defaultFilter != null) {
			builder.selectUnit(defaultFilter);
		}

		return builder;
	}

	static ImBuilderWrapper<EntityFilter> create() {
		return create(EntityFilter.NONE.instance());
	}
}
