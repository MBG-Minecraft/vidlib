package dev.beast.mods.shimmer.math;

import org.joml.Vector3f;

import java.util.Arrays;

public record SplitBox(Split split, int index, float centerX, float centerY, float centerZ, UV[] uvs, Vector3f[][] facePos) {
	@Override
	public String toString() {
		return "SplitBox#" + index + "@" + centerX + ";" + centerY + ";" + centerZ + "=" + Arrays.toString(uvs);
	}

	public int key() {
		return (split.id << 7) | index;
	}
}
