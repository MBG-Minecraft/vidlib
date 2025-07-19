package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.canvas.Canvas;

public interface VLWithCanvas {
	default void vl$setCanvas(Canvas canvas) {
	}
}
