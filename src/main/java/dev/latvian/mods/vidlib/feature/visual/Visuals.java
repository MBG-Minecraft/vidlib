package dev.latvian.mods.vidlib.feature.visual;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.math.Line;
import dev.latvian.mods.klib.math.Rotation;
import dev.latvian.mods.klib.math.VoxelShapeBox;
import dev.latvian.mods.klib.shape.PositionedColoredShape;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

public record Visuals(List<CubeVisual> cubes, List<LineVisual> lines, List<ResolvedTexturedCube> texturedCubes, List<PositionedColoredShape> shapes, List<PositionedColoredShape> brightShapes, List<PositionedColoredShape> outlineShapes) {
	public static final Visuals NONE = new Visuals(List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
	public static final Visuals FRAME_DEBUG = new Visuals();
	public static final Visuals TICK_DEBUG = new Visuals();

	@ApiStatus.Internal
	public static final Visuals TEMP = new Visuals();

	public Visuals() {
		this(new ArrayList<>(0), new ArrayList<>(0), new ArrayList<>(0), new ArrayList<>(0), new ArrayList<>(0), new ArrayList<>(0));
	}

	public boolean hasAny() {
		return !cubes.isEmpty() || !lines.isEmpty() || !texturedCubes.isEmpty() || !shapes.isEmpty() || !brightShapes.isEmpty() || !outlineShapes.isEmpty();
	}

	public void clear() {
		cubes.clear();
		lines.clear();
		texturedCubes.clear();
		shapes.clear();
		brightShapes.clear();
		outlineShapes.clear();
	}

	public void copyFrom(Visuals from) {
		cubes.addAll(from.cubes);
		lines.addAll(from.lines);
		texturedCubes.addAll(from.texturedCubes);
		shapes.addAll(from.shapes);
		brightShapes.addAll(from.brightShapes);
		outlineShapes.addAll(from.outlineShapes);
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

	public void addBright(PositionedColoredShape shape) {
		brightShapes.add(shape);
	}

	public void addOutline(PositionedColoredShape shape) {
		outlineShapes.add(shape);
	}

	public boolean contains(BlockPos pos) {
		return !cubes.isEmpty();
	}
}
