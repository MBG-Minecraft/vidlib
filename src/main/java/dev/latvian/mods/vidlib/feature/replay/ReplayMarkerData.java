package dev.latvian.mods.vidlib.feature.replay;

import dev.latvian.mods.klib.color.Color;

import java.util.Optional;

public record ReplayMarkerData(ReplayMarkerType type, Color color, Optional<MarkerPosition> position, String description) {
	public static final Color CHANGED_DIMENSION_COLOR = Color.ofRGB(0xAA00AA);
}
