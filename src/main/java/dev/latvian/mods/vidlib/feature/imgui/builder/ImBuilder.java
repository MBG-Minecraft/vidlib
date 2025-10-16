package dev.latvian.mods.vidlib.feature.imgui.builder;

import com.mojang.serialization.DynamicOps;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.node.Node;
import dev.latvian.mods.vidlib.feature.imgui.node.NodePin;
import imgui.ImGui;
import imgui.type.ImBoolean;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public interface ImBuilder<T> {
	record Unit<T>(String name, T value) implements ImBuilder<T> {
		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			return ImUpdate.NONE;
		}

		@Override
		public T build() {
			return value;
		}

		@Override
		public String getDisplayName() {
			return name;
		}
	}

	default void set(@Nullable T value) {
	}

	ImUpdate imgui(ImGraphics graphics);

	default ImUpdate nodeImgui(ImGraphics graphics) {
		return imgui(graphics);
	}

	default ImUpdate imguiKey(ImGraphics graphics, String label, String id) {
		if (!label.isEmpty()) {
			ImGui.alignTextToFramePadding();
			graphics.redTextIf(label, !isValid());

			if (!(this instanceof CompoundImBuilder)) {
				ImGui.sameLine();
			}
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

	default boolean equals(T a, T b) {
		return Objects.equals(a, b);
	}

	default boolean isSmall() {
		return false;
	}

	default <O> String toString(DynamicOps<O> ops, T value) {
		return String.valueOf(value);
	}

	default String getDisplayName() {
		return "Unknown";
	}

	default List<NodePin> getNodePins() {
		return List.of();
	}

	@Nullable
	default Node asNode() {
		var pins = getNodePins();
		return pins.isEmpty() ? null : new Node(this, pins);
	}
}
