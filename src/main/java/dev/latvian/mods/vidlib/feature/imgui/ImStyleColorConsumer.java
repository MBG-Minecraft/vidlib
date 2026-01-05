package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.klib.color.Color;
import imgui.flag.ImGuiCol;

public interface ImStyleColorConsumer {
	void setStyleCol(int key, int r, int g, int b, int a);

	default void setStyleCol(int key, float r, float g, float b, float a) {
		setStyleCol(key, (int) (r * 255F), (int) (g * 255F), (int) (b * 255F), (int) (a * 255F));
	}

	default void setStyleCol(int key, int argb) {
		setStyleCol(key, (argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, (argb >> 24) & 0xFF);
	}

	default void setStyleCol(int key, Color value) {
		setStyleCol(key, value.red(), value.green(), value.blue(), value.alpha());
	}

	default void setText(ImColorVariant variant) {
		setStyleCol(ImGuiCol.Text, variant.textColor);
	}

	default void setWarningText() {
		setText(ImColorVariant.YELLOW);
	}

	default void setErrorText() {
		setText(ImColorVariant.RED);
	}

	default void setSuccessText() {
		setText(ImColorVariant.GREEN);
	}

	default void setInfoText() {
		setText(ImColorVariant.BLUE);
	}

	default void setButton(ImColorVariant variant) {
		setStyleCol(ImGuiCol.Button, variant.color);
		setStyleCol(ImGuiCol.ButtonHovered, variant.hoverColor);
		setStyleCol(ImGuiCol.ButtonActive, variant.activeColor);
	}

	default void setButtonColor(Color col) {
		setStyleCol(ImGuiCol.Button, col);
		setStyleCol(ImGuiCol.ButtonHovered, col.lerp(0.3F, Color.WHITE));
		setStyleCol(ImGuiCol.ButtonActive, col.lerp(0.1F, Color.WHITE));
	}

	default void setRedButton() {
		setButton(ImColorVariant.RED);
	}

	default void setGreenButton() {
		setButton(ImColorVariant.GREEN);
	}
}
