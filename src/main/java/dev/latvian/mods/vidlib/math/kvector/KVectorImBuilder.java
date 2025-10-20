package dev.latvian.mods.vidlib.math.kvector;

import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderType;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import org.jetbrains.annotations.Nullable;

public interface KVectorImBuilder extends ImBuilderWithHolder<KVector> {
	ImBuilderWrapper.Factory<KVector> IMGUI_BUILDER_FACTORY = new ImBuilderWrapper.Factory<>(KVectorImBuilderEvent::new);
	ImBuilderType<KVector> TYPE = KVectorImBuilder::create;

	static ImBuilder<KVector> create(@Nullable KVector defaultValue) {
		var builder = IMGUI_BUILDER_FACTORY.get();

		if (defaultValue != null) {
			builder.set(defaultValue);
		}

		return builder;
	}

	static ImBuilder<KVector> create() {
		return create(null);
	}

	@Override
	default String resolve(KNumberContext ctx) {
		return isValid() ? String.valueOf(build().get(ctx)) : "Invalid";
	}
}
