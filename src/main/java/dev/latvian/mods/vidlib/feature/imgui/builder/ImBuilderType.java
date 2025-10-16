package dev.latvian.mods.vidlib.feature.imgui.builder;

import java.util.function.Supplier;

@FunctionalInterface
public interface ImBuilderType<T> extends Supplier<ImBuilder<T>> {
	record Unit<T>(ImBuilder<T> unit) implements ImBuilderType<T> {
		@Override
		public ImBuilder<T> get() {
			return unit;
		}
	}
}
