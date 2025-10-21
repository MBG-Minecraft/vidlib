package dev.latvian.mods.vidlib.math.knumber;

import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderType;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;

public interface KNumberImBuilder extends ImBuilderWithHolder<KNumber> {
	ImBuilderWrapper.Factory<KNumber> IMGUI_BUILDER_FACTORY = new ImBuilderWrapper.Factory<>(KNumberImBuilderEvent::new);
	ImBuilderType<KNumber> TYPE = () -> create(0D);

	static ImBuilder<KNumber> create(KNumber defaultValue) {
		var builder = IMGUI_BUILDER_FACTORY.get();

		if (defaultValue != null) {
			builder.set(defaultValue);
		}

		return builder;
	}

	static ImBuilder<KNumber> create(double defaultValue) {
		return create(KNumber.of(defaultValue));
	}

	@Override
	default String resolve(KNumberContext ctx) {
		return isValid() ? String.valueOf(build().get(ctx)) : "Invalid";
	}
}
