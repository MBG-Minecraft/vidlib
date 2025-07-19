package dev.latvian.mods.vidlib.feature.imgui.builder;

import java.util.function.Supplier;

public record ImBuilderHolder<T>(String name, Supplier<ImBuilder<? extends T>> factory, boolean isDefault) {
	public ImBuilderHolder(String name, Supplier<ImBuilder<? extends T>> factory) {
		this(name, factory, false);
	}

	public ImBuilder<? extends T> get() {
		return factory.get();
	}
}
