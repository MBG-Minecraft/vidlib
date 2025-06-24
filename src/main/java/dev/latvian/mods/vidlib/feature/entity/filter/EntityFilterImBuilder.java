package dev.latvian.mods.vidlib.feature.entity.filter;

import dev.latvian.mods.vidlib.feature.imgui.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilderWrapper;
import org.jetbrains.annotations.Nullable;

public interface EntityFilterImBuilder extends ImBuilder<EntityFilter> {
	static ImBuilderWrapper<EntityFilter> create(@Nullable EntityFilter defaultFilter) {
		var builder = new ImBuilderWrapper<>(EntityFilter.IMGUI_BUILDERS);

		if (defaultFilter != null) {
			builder.selectUnit(defaultFilter);
		}

		return builder;
	}

	static ImBuilderWrapper<EntityFilter> create() {
		return create(EntityFilter.NONE.instance());
	}
}
