package dev.latvian.mods.vidlib.feature.imgui;

public interface ImBuilder<T> {
	record Unit<T>(T value) implements ImBuilder<T> {
		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			return ImUpdate.NONE;
		}

		@Override
		public T build() {
			return value;
		}
	}

	default void set(T value) {
	}

	ImUpdate imgui(ImGraphics graphics);

	default boolean isValid() {
		return true;
	}

	T build();
}
