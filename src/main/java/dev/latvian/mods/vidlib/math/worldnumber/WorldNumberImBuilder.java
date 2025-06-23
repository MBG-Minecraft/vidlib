package dev.latvian.mods.vidlib.math.worldnumber;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilderWrapper;

public interface WorldNumberImBuilder extends ImBuilder<WorldNumber> {
	static ImBuilder<WorldNumber> create(double defaultValue) {
		var w = new ImBuilderWrapper<>(WorldNumber.IMGUI_BUILDERS);
		w.builder.set(Cast.to(FixedWorldNumber.of(defaultValue)));
		return w;
	}
}
