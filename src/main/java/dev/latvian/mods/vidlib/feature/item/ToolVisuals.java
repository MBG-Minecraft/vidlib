package dev.latvian.mods.vidlib.feature.item;

import dev.latvian.mods.kmath.Line;
import dev.latvian.mods.kmath.VoxelShapeBox;
import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.vidlib.feature.texture.ResolvedTexturedCube;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public record ToolVisuals(List<CubeVisual> cubes, List<LineVisual> lines, List<ResolvedTexturedCube> texturedCubes) {
	public static final ToolVisuals NONE = new ToolVisuals(List.of(), List.of(), List.of());

	public static ToolVisuals cube(Vec3 pos) {
		return new ToolVisuals(List.of(new CubeVisual(pos)), List.of(), List.of());
	}

	public static ToolVisuals texturedCube(ResolvedTexturedCube cube) {
		return new ToolVisuals(List.of(), List.of(), List.of(cube));
	}

	public record CubeVisual(Vec3 pos, VoxelShapeBox shape, Color color, Color lineColor) {
		public CubeVisual(Vec3 pos) {
			this(pos, VoxelShapeBox.FULL, Color.CYAN, Color.WHITE);
		}
	}

	public record LineVisual(Line line, Color startColor, Color endColor) {
		public LineVisual(Line line, Color color) {
			this(line, color, color);
		}

		public LineVisual(Line line) {
			this(line, Color.WHITE);
		}
	}

	public ToolVisuals() {
		this(new ArrayList<>(0), new ArrayList<>(0), new ArrayList<>(0));
	}

	public boolean contains(BlockPos pos) {
		return !cubes.isEmpty();
	}
}
