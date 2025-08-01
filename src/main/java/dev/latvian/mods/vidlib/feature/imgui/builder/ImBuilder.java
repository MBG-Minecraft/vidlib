package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;
import imgui.type.ImBoolean;

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
			graphics.redTextIf(label, !isValid());
			ImGui.sameLine();
		}

		if (!id.isEmpty()) {
			ImGui.pushID("###" + id);
		}

		var update = ImUpdate.NONE;

		try {
			update = imgui(graphics);
		} catch (Throwable ex) {
			graphics.stackTrace(ex);
		}

		if (!id.isEmpty()) {
			ImGui.popID();
		}

		return update;
	}

	default ImUpdate imguiOptionalKey(ImGraphics graphics, ImBoolean enabled, String label, String id) {
		var update = ImUpdate.full(ImGui.checkbox(label + "###" + id + "-enabled", enabled));

		if (enabled.get()) {
			ImGui.sameLine();
			update = update.or(imguiKey(graphics, "", id));
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
