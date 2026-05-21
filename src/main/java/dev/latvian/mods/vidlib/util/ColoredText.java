package dev.latvian.mods.vidlib.util;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import imgui.flag.ImGuiCol;
import org.jetbrains.annotations.Nullable;

public record ColoredText(String text, @Nullable Color color) {
	public static final ColoredText EMPTY = new ColoredText("", null);

	public static final Color WARNING = new Color(0xFFFFFF55);
	public static final Color ERROR = new Color(0xFFFF5555);
	public static final Color SUCCESS = new Color(0xFF8CFF95);
	public static final Color INFO = new Color(0xFF63BEFF);

	public static ColoredText of(String text) {
		return text.isEmpty() ? EMPTY : new ColoredText(text, null);
	}

	public static ColoredText of(String text, @Nullable Color color) {
		return new ColoredText(text, color);
	}

	public static ColoredText warning(String text) {
		return new ColoredText(text, WARNING);
	}

	public static ColoredText error(String text) {
		return new ColoredText(text, ERROR);
	}

	public static ColoredText info(String text) {
		return new ColoredText(text, INFO);
	}

	public ColoredText withColor(@Nullable Color color) {
		return new ColoredText(text, color);
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
