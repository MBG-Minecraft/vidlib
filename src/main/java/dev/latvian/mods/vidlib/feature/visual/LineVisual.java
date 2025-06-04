package dev.latvian.mods.vidlib.feature.visual;

import dev.latvian.mods.kmath.Line;
import dev.latvian.mods.kmath.color.Color;

public record LineVisual(Line line, Color startColor, Color endColor) implements Visual {
}
