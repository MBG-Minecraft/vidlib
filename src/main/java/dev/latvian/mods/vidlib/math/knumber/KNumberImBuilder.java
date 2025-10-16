package dev.latvian.mods.vidlib.math.knumber;

import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;

public interface KNumberImBuilder extends ImBuilderWithHolder<KNumber> {
	ImBuilderWrapper.Factory<KNumber> IMGUI_BUILDER_FACTORY = new ImBuilderWrapper.Factory<>(KNumberImBuilderEvent::new);

	static ImBuilder<KNumber> create(KNumber defaultValue) {
		return IMGUI_BUILDER_FACTORY.create(defaultValue);
	}

	static ImBuilder<KNumber> create(double defaultValue) {
		return create(KNumber.of(defaultValue));
	}
}
