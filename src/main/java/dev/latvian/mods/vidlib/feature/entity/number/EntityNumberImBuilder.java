package dev.latvian.mods.vidlib.feature.entity.number;

import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderType;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import org.jetbrains.annotations.Nullable;

public interface EntityNumberImBuilder extends ImBuilderWithHolder<EntityNumber> {
	ImBuilderWrapper.Factory<EntityNumber> IMGUI_BUILDER_FACTORY = new ImBuilderWrapper.Factory<>(EntityNumberImBuilderEvent::new);
	ImBuilderType<EntityNumber> TYPE = EntityNumberImBuilder::create;

	static ImBuilder<EntityNumber> create(@Nullable EntityNumber defaultValue) {
		var builder = IMGUI_BUILDER_FACTORY.get();

		if (defaultValue != null) {
			builder.set(defaultValue);
		}

		return builder;
	}

	static ImBuilder<EntityNumber> create() {
		return create(EntityNumber.Y.instance());
	}
}
