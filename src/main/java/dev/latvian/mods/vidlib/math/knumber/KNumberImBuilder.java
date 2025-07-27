package dev.latvian.mods.vidlib.math.knumber;

import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;

public interface KNumberImBuilder extends ImBuilder<KNumber> {
	ImBuilderWrapper.Factory<KNumber> IMGUI_BUILDER_FACTORY = new ImBuilderWrapper.Factory<>(KNumberImBuilderEvent::new);

	static ImBuilderWrapper<KNumber> create(double defaultValue) {
		var w = new ImBuilderWrapper<>(IMGUI_BUILDER_FACTORY);

		if (defaultValue == 1D) {
			// w.selectUnit(KNumber.ONE);
		} else if (defaultValue != 0D) {
			// w.set(KNumber.of(defaultValue));
		}

		return w;
	}
}
