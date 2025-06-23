package dev.latvian.mods.vidlib.math.worldnumber;

import dev.latvian.mods.vidlib.feature.imgui.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilderWrapper;

public interface WorldNumberImBuilder extends ImBuilder<WorldNumber> {
	static ImBuilder<WorldNumber> create(double defaultValue) {
		var w = new ImBuilderWrapper<>(WorldNumber.IMGUI_BUILDERS);

		if (defaultValue != 0D) {
			w.set(FixedWorldNumber.of(defaultValue));
		}

		return w;
	}
}
