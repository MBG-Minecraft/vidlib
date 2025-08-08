package dev.latvian.mods.vidlib.feature.imgui.builder;

public record ImBuilderHolder<T>(String name, ImBuilderType<T> factory, boolean isDefault) {
	public ImBuilderHolder(String name, ImBuilderType<T> factory) {
		this(name, factory, false);
	}

	public ImBuilder<? extends T> get() {
		return factory.get();
	}
}
