package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.klib.color.Color;
import imgui.extension.imnodes.flag.ImNodesCol;

public interface ImNodesStyleColorConsumer {
	void setNodesStyleCol(int key, int r, int g, int b, int a);

	default void setNodesStyleCol(int key, float r, float g, float b, float a) {
		setNodesStyleCol(key, (int) (r * 255F), (int) (g * 255F), (int) (b * 255F), (int) (a * 255F));
	}

	default void setNodesStyleCol(int key, int argb) {
		setNodesStyleCol(key, (argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, (argb >> 24) & 0xFF);
	}

	default void setNodesStyleCol(int key, Color value) {
		setNodesStyleCol(key, value.red(), value.green(), value.blue(), value.alpha());
	}

	default void setNodesPin(ImColorVariant variant) {
		setNodesStyleCol(ImNodesCol.Pin, variant.color);
		setNodesStyleCol(ImNodesCol.PinHovered, variant.hoverColor);
	}

	default void setNodesLink(ImColorVariant variant) {
		setNodesStyleCol(ImNodesCol.Link, variant.color);
		setNodesStyleCol(ImNodesCol.LinkHovered, variant.hoverColor);
		setNodesStyleCol(ImNodesCol.LinkSelected, variant.activeColor);
	}
}
