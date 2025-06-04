package dev.latvian.mods.vidlib.feature.visual;

import dev.latvian.mods.kmath.Line;
import dev.latvian.mods.kmath.Rotation;
import dev.latvian.mods.kmath.VoxelShapeBox;
import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.kmath.shape.PositionedColoredShape;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public record Visuals(List<CubeVisual> cubes, List<LineVisual> lines, List<ResolvedTexturedCube> texturedCubes, List<PositionedColoredShape> shapes) {
	public static final Visuals NONE = new Visuals(List.of(), List.of(), List.of(), List.of());

	public Visuals() {
		this(new ArrayList<>(0), new ArrayList<>(0), new ArrayList<>(0), new ArrayList<>(0));
	}

	public void addCube(Vec3 pos, VoxelShapeBox shape, Color color, Color lineColor, Rotation rotation) {
		cubes.add(new CubeVisual(pos, shape, color, lineColor, rotation));
	}

	public void addCube(Vec3 pos) {
		addCube(pos, VoxelShapeBox.CENTERED, Color.CYAN, Color.WHITE, Rotation.NONE);
	}

	public void addLine(Line line, Color startColor, Color endColor) {
		lines.add(new LineVisual(line, startColor, endColor));
	}

	public void addLine(Line line, Color color) {
		lines.add(new LineVisual(line, color, color));
	}

	public void addLine(Line line) {
		lines.add(new LineVisual(line, Color.WHITE, Color.WHITE));
	}

	public void add(PositionedColoredShape shape) {
		shapes.add(shape);
	}

	public boolean contains(BlockPos pos) {
		return !cubes.isEmpty();
	}
}
