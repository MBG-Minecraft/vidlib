package dev.latvian.mods.vidlib.feature.imgui.builder.interpolation;

import dev.latvian.mods.klib.interpolation.Interpolation;
import dev.latvian.mods.klib.interpolation.LinearInterpolation;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderType;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import org.jetbrains.annotations.Nullable;

public interface InterpolationImBuilder extends ImBuilder<Interpolation> {
	ImBuilderWrapper.Factory<Interpolation> IMGUI_BUILDER_FACTORY = new ImBuilderWrapper.Factory<>(InterpolationImBuilderEvent::new);
	ImBuilderType<Interpolation> TYPE = InterpolationImBuilder::create;

	static ImBuilder<Interpolation> create(@Nullable Interpolation defaultValue) {
		var builder = IMGUI_BUILDER_FACTORY.get();

		if (defaultValue != null) {
			builder.set(defaultValue);
		}

		return builder;
	}

	static ImBuilder<Interpolation> create() {
		return create(LinearInterpolation.INSTANCE);
	}
}
