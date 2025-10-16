package dev.latvian.mods.vidlib.feature.imgui.builder;

public interface ImBuilderWithHolder<T> extends ImBuilder<T> {
	interface Factory {
		ImBuilderWithHolder<?> createImBuilder();
	}

	ImBuilderHolder<?> holder();

	@Override
	default String getDisplayName() {
		return holder().name();
	}
}
