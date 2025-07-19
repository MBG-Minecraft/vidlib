package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;

import java.util.Objects;

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

	default ImUpdate imguiKey(ImGraphics graphics, String label, String id) {
		if (!label.isEmpty()) {
			ImGui.alignTextToFramePadding();
			ImGui.text(label);
			ImGui.sameLine();
		}

		if (!id.isEmpty()) {
			ImGui.pushID("###" + id);
		}

		var update = imgui(graphics);

		if (!id.isEmpty()) {
			ImGui.popID();
		}

		return update;
	}

	default boolean isValid() {
		return true;
	}

	T build();

	default boolean isSmall() {
		return false;
	}

	default boolean equals(T a, T b) {
		return Objects.equals(a, b);
	}
}
