package dev.latvian.mods.vidlib.feature.imgui.builder.interpolation;

import dev.latvian.mods.klib.interpolation.Interpolation;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderType;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import org.jetbrains.annotations.Nullable;

public interface InterpolationImBuilder extends ImBuilder<Ref<Interpolation>> {
	ImBuilderWrapper.Factory<Ref<Interpolation>> IMGUI_BUILDER_FACTORY = new ImBuilderWrapper.Factory<>(InterpolationImBuilderEvent::new);
	ImBuilderType<Ref<Interpolation>> TYPE = InterpolationImBuilder::create;

	static ImBuilder<Ref<Interpolation>> create(@Nullable Ref<Interpolation> defaultValue) {
		var builder = IMGUI_BUILDER_FACTORY.get();

		if (defaultValue != null) {
			builder.set(defaultValue);
		}

		return builder;
	}

	static ImBuilder<Ref<Interpolation>> create() {
		return create(Interpolation.linear());
	}
}
