package dev.latvian.mods.vidlib.math.kvector;

import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;

public interface KVectorImBuilder extends ImBuilder<KVector> {
	ImBuilderWrapper.Factory<KVector> IMGUI_BUILDER_FACTORY = new ImBuilderWrapper.Factory<>(KVectorImBuilderEvent::new);

	static ImBuilderWrapper<KVector> create() {
		return new ImBuilderWrapper<>(IMGUI_BUILDER_FACTORY);
	}
}
