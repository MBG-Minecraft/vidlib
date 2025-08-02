package dev.latvian.mods.vidlib.feature.imgui.builder;

public record ImBuilderHolder<T>(String name, ImBuilderSupplier<T> factory, boolean isDefault) {
	public ImBuilderHolder(String name, ImBuilderSupplier<T> factory) {
		this(name, factory, false);
	}

	public ImBuilder<? extends T> get() {
		return factory.get();
	}
}
