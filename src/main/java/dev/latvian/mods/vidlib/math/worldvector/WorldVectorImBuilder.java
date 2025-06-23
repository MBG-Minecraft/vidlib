package dev.latvian.mods.vidlib.math.worldvector;

import dev.latvian.mods.vidlib.feature.imgui.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilderWrapper;

public interface WorldVectorImBuilder extends ImBuilder<WorldVector> {
	static ImBuilder<WorldVector> create() {
		return new ImBuilderWrapper<>(WorldVector.IMGUI_BUILDERS);
	}
}
