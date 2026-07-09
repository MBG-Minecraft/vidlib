package dev.latvian.mods.vidlib.feature.entity.progress;

import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.resources.Identifier;

public record ProgressBarType(
	ProgressBarTextures textures,
	int textureWidth,
	int textureHeight,
	int centerX,
	int centerY,
	int barStart,
	int barWidth,
	int offset,
	float scale
) {
	public static ProgressBarType vanilla(Identifier id) {
		return new ProgressBarType(new ProgressBarTextures(id), 256, 8, 91, 3, 0, 182, 12, 1F);
	}

	public static final ProgressBarType BLUE = vanilla(ID.vidlib("blue"));
	public static final ProgressBarType GREEN = vanilla(ID.vidlib("green"));
	public static final ProgressBarType PINK = vanilla(ID.vidlib("pink"));
	public static final ProgressBarType PURPLE = vanilla(ID.vidlib("purple"));
	public static final ProgressBarType RED = vanilla(ID.vidlib("red"));
	public static final ProgressBarType WHITE = vanilla(ID.vidlib("white"));
	public static final ProgressBarType YELLOW = vanilla(ID.vidlib("yellow"));

	public int width() {
		return centerX * 2;
	}

	public int height() {
		return Math.min(textureHeight, centerY * 2);
	}
}
