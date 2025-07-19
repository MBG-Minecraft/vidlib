package dev.latvian.mods.vidlib.math.knumber;

import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;

public interface KNumberImBuilder extends ImBuilder<KNumber> {
	static ImBuilderWrapper<KNumber> create(double defaultValue) {
		var w = new ImBuilderWrapper<>(KNumber.IMGUI_BUILDERS);

		if (defaultValue != 0D) {
			w.set(KNumber.of(defaultValue));
		}

		return w;
	}
}
