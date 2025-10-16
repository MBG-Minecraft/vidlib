package dev.latvian.mods.vidlib.feature.entity.filter;

import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import org.jetbrains.annotations.Nullable;

public interface EntityFilterImBuilder extends ImBuilderWithHolder<EntityFilter> {
	ImBuilderWrapper.Factory<EntityFilter> IMGUI_BUILDER_FACTORY = new ImBuilderWrapper.Factory<>(EntityFilterImBuilderEvent::new);

	static ImBuilder<EntityFilter> create(@Nullable EntityFilter defaultFilter) {
		return IMGUI_BUILDER_FACTORY.create(defaultFilter);
	}

	static ImBuilder<EntityFilter> create() {
		return create(EntityFilter.NONE.instance());
	}
}
