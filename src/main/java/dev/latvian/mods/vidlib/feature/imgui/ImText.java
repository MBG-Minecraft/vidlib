package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.klib.color.Color;
import imgui.flag.ImGuiCol;
import org.jetbrains.annotations.Nullable;

public record ImText(String text, @Nullable Color color) {
	public static final ImText EMPTY = new ImText("", null);

	public static final Color WARNING = new Color(0xFFFFFF55);
	public static final Color ERROR = new Color(0xFFFF5555);
	public static final Color SUCCESS = new Color(0xFF8CFF95);
	public static final Color INFO = new Color(0xFF63BEFF);

	public static ImText of(String text) {
		return text.isEmpty() ? EMPTY : new ImText(text, null);
	}

	public static ImText of(String text, @Nullable Color color) {
		return new ImText(text, color);
	}

	public static ImText warning(String text) {
		return new ImText(text, WARNING);
	}

	public static ImText error(String text) {
		return new ImText(text, ERROR);
	}

	public static ImText info(String text) {
		return new ImText(text, INFO);
	}

	public boolean pushStack() {
		return color != null;
	}

	public void push(ImGraphics graphics) {
		if (pushStack()) {
			graphics.pushStack();

			if (color == WARNING) {
				graphics.setWarningText();
			} else if (color == ERROR) {
				graphics.setErrorText();
			} else if (color == SUCCESS) {
				graphics.setSuccessText();
			} else if (color == INFO) {
				graphics.setInfoText();
			} else if (color != null) {
				graphics.setStyleCol(ImGuiCol.Text, color);
			}
		}
	}

	public void pop(ImGraphics graphics) {
		if (pushStack()) {
			graphics.popStack();
		}
	}
}
