package dev.latvian.mods.vidlib.core;

import com.mojang.blaze3d.platform.Window;

public interface VLWindow {
	default void vl$setViewportArea(double xOffset, double yOffset, double xScale, double yScale) {
		// NO-OP
	}

	default double vl$getXOffset() {
		return 0; // NO-OP
	}

	default double vl$getYOffset() {
		return 0; // NO-OP
	}

	default double vl$getInverseYOffset() {
		return 0; // NO-OP
	}

	default int vl$getUnscaledWidth() {
		return 1; // NO-OP
	}

	default int vl$getUnscaledHeight() {
		return 1; // NO-OP
	}

	default int vl$getUnscaledFramebufferWidth() {
		return 1; // NO-OP
	}

	default int vl$getUnscaledFramebufferHeight() {
		return 1; // NO-OP
	}

	default double vl$modifyCursorX(double x) {
		return x - vl$getXOffset() * vl$getUnscaledWidth();
	}

	default double vl$modifyCursorY(double y) {
		return y - vl$getYOffset() * vl$getUnscaledHeight();
	}

	default boolean isInvisible() {
		var w = (Window) this;
		return w.isMinimized() || w.getGuiScaledWidth() < 1 || w.getGuiScaledHeight() < 1;
	}
}
