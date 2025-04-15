package dev.beast.mods.shimmer.feature.item;

import dev.latvian.mods.kmath.Line;
import dev.latvian.mods.kmath.VoxelShapeBox;
import dev.latvian.mods.kmath.color.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record ToolVisuals(List<CubeVisual> cubes, List<LineVisual> lines) {
	public static final ToolVisuals NONE = new ToolVisuals(List.of(), List.of());

	public static ToolVisuals cube(Vec3 pos) {
		return new ToolVisuals(List.of(new CubeVisual(pos)), List.of());
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

	public boolean contains(BlockPos pos) {
		return !cubes.isEmpty();
	}
}
