package dev.latvian.mods.vidlib.feature.imgui;

public interface ImBuilder<T> {
	default void set(T value) {
	}

	ImUpdate imgui(ImGraphics graphics);

	default boolean isValid() {
		return true;
	}

	T build();
}
