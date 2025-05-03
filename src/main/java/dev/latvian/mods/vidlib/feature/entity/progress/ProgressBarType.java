package dev.latvian.mods.vidlib.feature.entity.progress;

import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.resources.ResourceLocation;

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
	public static ProgressBarType vanilla(ResourceLocation id) {
		return new ProgressBarType(new ProgressBarTextures(id), 256, 8, 91, 3, 0, 182, 12, 1F);
	}

	public static final ProgressBarType BLUE = vanilla(VidLib.id("blue"));
	public static final ProgressBarType GREEN = vanilla(VidLib.id("green"));
	public static final ProgressBarType PINK = vanilla(VidLib.id("pink"));
	public static final ProgressBarType PURPLE = vanilla(VidLib.id("purple"));
	public static final ProgressBarType RED = vanilla(VidLib.id("red"));
	public static final ProgressBarType WHITE = vanilla(VidLib.id("white"));
	public static final ProgressBarType YELLOW = vanilla(VidLib.id("yellow"));

	public int width() {
		return centerX * 2;
	}

	public int height() {
		return centerY * 2;
	}
}
