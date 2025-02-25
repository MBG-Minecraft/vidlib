package dev.beast.mods.shimmer.content.clock;

import dev.beast.mods.shimmer.Shimmer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public enum ClockType implements StringRepresentable {
	CLOCK_3x7("3x7", 3, 7, 0, 62, 0, 1),
	CLOCK_4x7("4x7", 4, 7, 8, 58, 0, 2),
	CLOCK_5x7("5x7", 5, 7, 16, 62, 0, 1);

	public static final ResourceLocation TEXTURE = Shimmer.id("textures/misc/clocks.png");
	public static final float TEXTURE_W = 64F;
	public static final float TEXTURE_H = 32F;

	public final String name;
	public final int width;
	public final int height;
	public final int v0;
	public final int colonWidth;
	public final float[][] uvs;

	ClockType(String name, int width, int height, int v0, int colonX, int colonY, int colonWidth) {
		this.name = name;
		this.width = width;
		this.height = height;
		this.v0 = v0;
		this.colonWidth = colonWidth;
		this.uvs = new float[11][4];

		for (int i = 0; i < 10; i++) {
			uvs[i][0] = (i * (width + 1F)) / TEXTURE_W;
			uvs[i][1] = (v0) / TEXTURE_H;
			uvs[i][2] = ((i * (width + 1F)) + width) / TEXTURE_W;
			uvs[i][3] = (v0 + height) / TEXTURE_H;
		}

		uvs[10][0] = colonX / TEXTURE_W;
		uvs[10][1] = colonY / TEXTURE_H;
		uvs[10][2] = (colonX + colonWidth) / TEXTURE_W;
		uvs[10][3] = (colonY + height) / TEXTURE_H;
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	public int getWidth(char[] string) {
		if (string.length == 0) {
			return 0;
		}

		int w = 1;

		for (char c : string) {
			if (c == ':') {
				w += colonWidth + 1;
			} else {
				w += width + 1;
			}
		}

		return w;
	}
}

