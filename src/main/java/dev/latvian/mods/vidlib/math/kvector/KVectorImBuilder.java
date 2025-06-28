package dev.latvian.mods.vidlib.math.kvector;

import dev.latvian.mods.vidlib.feature.imgui.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilderWrapper;

public interface KVectorImBuilder extends ImBuilder<KVector> {
	static ImBuilderWrapper<KVector> create() {
		return new ImBuilderWrapper<>(KVector.IMGUI_BUILDERS);
	}
}
