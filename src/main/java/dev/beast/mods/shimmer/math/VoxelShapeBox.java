package dev.beast.mods.shimmer.math;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

public record VoxelShapeBox(List<Line> edges, List<AABB> boxes) {
	public static final VoxelShapeBox EMPTY = new VoxelShapeBox(List.of(), List.of());

	public static VoxelShapeBox of(VoxelShape shape) {
		if (shape.isEmpty()) {
			return EMPTY;
		}

		var edges = new ArrayList<Line>();
		var boxes = new ArrayList<AABB>();
		shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> boxes.add(new AABB(minX, minY, minZ, maxX, maxY, maxZ)));
		shape.forAllEdges((minX, minY, minZ, maxX, maxY, maxZ) -> edges.add(new Line(new Vec3(minX, minY, minZ), new Vec3(maxX, maxY, maxZ))));
		return edges.isEmpty() && boxes.isEmpty() ? EMPTY : new VoxelShapeBox(List.copyOf(edges), List.copyOf(boxes));
	}
}
