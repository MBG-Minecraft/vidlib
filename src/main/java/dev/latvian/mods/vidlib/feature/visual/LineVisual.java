package dev.latvian.mods.vidlib.feature.visual;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.math.Line;

public record LineVisual(Line line, Color startColor, Color endColor) implements Visual {
}
