package dev.latvian.mods.vidlib.feature.imgui;

import imgui.ImGui;

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
		ImGui.alignTextToFramePadding();
		ImGui.text(label);
		ImGui.sameLine();
		ImGui.pushID(id);
		var update = imgui(graphics);
		ImGui.popID();
		return update;
	}

	default boolean isValid() {
		return true;
	}

	T build();
}
