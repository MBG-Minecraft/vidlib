package dev.latvian.mods.vidlib.feature.structure;

import dev.latvian.mods.klib.math.AAIBB;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public final class GhostStructureCollision {
	private static final BlockState AIR = Blocks.AIR.defaultBlockState();
	private static final FluidState EMPTY_FLUID = Fluids.EMPTY.defaultFluidState();
	private static Level level;
	private static String signature = "";
	private static Long2ObjectMap<List<VoxelShape>> sections = new Long2ObjectOpenHashMap<>();
	private static AABB bounds;
	private static boolean empty = true;
	private static Thread updateThread;
	private static boolean fedThisMove;
	private GhostStructureCollision() {
	}

	public static void update(Level newLevel, List<Placement> placements) {
		if (placements.isEmpty() && empty && newLevel == level) {
			return;
		}

		var builder = new StringBuilder();

		for (var placement : placements) {
			placement.appendSignature(builder);
		}

		var newSignature = builder.toString();

		if (newLevel == level && newSignature.equals(signature)) {
			return;
		}

		level = newLevel;
		updateThread = Thread.currentThread();
		signature = newSignature;
		rebuild(placements);
	}

	private static void rebuild(List<Placement> placements) {
		var map = new Long2ObjectOpenHashMap<List<VoxelShape>>();
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double minZ = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		double maxZ = Double.NEGATIVE_INFINITY;

		for (var placement : placements) {
			var holder = placement.holder();
			var shapeLevel = new CollisionLevel(holder.blocks());
			var rotation = new Quaternionf()
				.rotationY(placement.rotationY())
				.rotateX(placement.rotationX())
				.rotateZ(placement.rotationZ());
			boolean identityRotation = placement.rotationX() == 0F && placement.rotationY() == 0F && placement.rotationZ() == 0F;
			var placementBounds = placement.bounds();

			for (var entry : holder.blocks().long2ObjectEntrySet()) {
				var state = entry.getValue();

				if (state.isAir()) {
					continue;
				}

				int localX = BlockPos.getX(entry.getLongKey());
				int localY = BlockPos.getY(entry.getLongKey());
				int localZ = BlockPos.getZ(entry.getLongKey());

				if (placementBounds != null && !placementBounds.contains(localX, localY, localZ)) {
					continue;
				}

				var blockShape = state.getCollisionShape(shapeLevel, new BlockPos(localX, localY, localZ));

				if (blockShape.isEmpty()) {
					continue;
				}

				for (var box : blockShape.toAabbs()) {
					double x0 = (localX + box.minX + placement.centerX()) * placement.scaleX();
					double y0 = (localY + box.minY + placement.centerY()) * placement.scaleY();
					double z0 = (localZ + box.minZ + placement.centerZ()) * placement.scaleZ();
					double x1 = (localX + box.maxX + placement.centerX()) * placement.scaleX();
					double y1 = (localY + box.maxY + placement.centerY()) * placement.scaleY();
					double z1 = (localZ + box.maxZ + placement.centerZ()) * placement.scaleZ();
					double boxMinX, boxMinY, boxMinZ, boxMaxX, boxMaxY, boxMaxZ;

					if (identityRotation) {
						boxMinX = Math.min(x0, x1);
						boxMinY = Math.min(y0, y1);
						boxMinZ = Math.min(z0, z1);
						boxMaxX = Math.max(x0, x1);
						boxMaxY = Math.max(y0, y1);
						boxMaxZ = Math.max(z0, z1);
					} else {
						boxMinX = boxMinY = boxMinZ = Double.POSITIVE_INFINITY;
						boxMaxX = boxMaxY = boxMaxZ = Double.NEGATIVE_INFINITY;

						for (int corner = 0; corner < 8; corner++) {
							var vector = new Vector3f(
								(float) ((corner & 1) == 0 ? x0 : x1),
								(float) ((corner & 2) == 0 ? y0 : y1),
								(float) ((corner & 4) == 0 ? z0 : z1)
							);
							rotation.transform(vector);

							boxMinX = Math.min(boxMinX, vector.x);
							boxMinY = Math.min(boxMinY, vector.y);
							boxMinZ = Math.min(boxMinZ, vector.z);
							boxMaxX = Math.max(boxMaxX, vector.x);
							boxMaxY = Math.max(boxMaxY, vector.y);
							boxMaxZ = Math.max(boxMaxZ, vector.z);
						}
					}

					var worldBox = new AABB(
						boxMinX + placement.x(),
						boxMinY + placement.y(),
						boxMinZ + placement.z(),
						boxMaxX + placement.x(),
						boxMaxY + placement.y(),
						boxMaxZ + placement.z()
					);

					addToSections(map, Shapes.create(worldBox));

					minX = Math.min(minX, worldBox.minX);
					minY = Math.min(minY, worldBox.minY);
					minZ = Math.min(minZ, worldBox.minZ);
					maxX = Math.max(maxX, worldBox.maxX);
					maxY = Math.max(maxY, worldBox.maxY);
					maxZ = Math.max(maxZ, worldBox.maxZ);
				}
			}
		}

		sections = map;
		empty = map.isEmpty();
		bounds = empty ? null : new AABB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	private static void addToSections(Long2ObjectOpenHashMap<List<VoxelShape>> map, VoxelShape shape) {
		var box = shape.bounds();
		int minSectionX = Mth.floor(box.minX) >> 4;
		int minSectionY = Mth.floor(box.minY) >> 4;
		int minSectionZ = Mth.floor(box.minZ) >> 4;
		int maxSectionX = Mth.floor(box.maxX) >> 4;
		int maxSectionY = Mth.floor(box.maxY) >> 4;
		int maxSectionZ = Mth.floor(box.maxZ) >> 4;

		for (int sectionX = minSectionX; sectionX <= maxSectionX; sectionX++) {
			for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
				for (int sectionZ = minSectionZ; sectionZ <= maxSectionZ; sectionZ++) {
					map.computeIfAbsent(SectionPos.asLong(sectionX, sectionY, sectionZ), key -> new ArrayList<>()).add(shape);
				}
			}
		}
	}

	public static List<VoxelShape> shapes(AABB box) {
		if (empty || Thread.currentThread() != updateThread || !bounds.intersects(box)) {
			return List.of();
		}

		int minSectionX = Mth.floor(box.minX) >> 4;
		int minSectionY = Mth.floor(box.minY) >> 4;
		int minSectionZ = Mth.floor(box.minZ) >> 4;
		int maxSectionX = Mth.floor(box.maxX) >> 4;
		int maxSectionY = Mth.floor(box.maxY) >> 4;
		int maxSectionZ = Mth.floor(box.maxZ) >> 4;

		if (minSectionX == maxSectionX && minSectionY == maxSectionY && minSectionZ == maxSectionZ) {
			var boxShapes = sections.get(SectionPos.asLong(minSectionX, minSectionY, minSectionZ));

			if (boxShapes == null) {
				return List.of();
			}

			List<VoxelShape> result = null;

			for (var shape : boxShapes) {
				if (shape.bounds().intersects(box)) {
					if (result == null) {
						result = new ArrayList<>();
					}

					result.add(shape);
				}
			}

			return result == null ? List.of() : result;
		}

		List<VoxelShape> result = null;
		ObjectOpenHashSet<VoxelShape> seen = null;

		for (int sectionX = minSectionX; sectionX <= maxSectionX; sectionX++) {
			for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
				for (int sectionZ = minSectionZ; sectionZ <= maxSectionZ; sectionZ++) {
					var boxShapes = sections.get(SectionPos.asLong(sectionX, sectionY, sectionZ));

					if (boxShapes == null) {
						continue;
					}

					for (var shape : boxShapes) {
						if (!shape.bounds().intersects(box)) {
							continue;
						}

						if (seen == null) {
							seen = new ObjectOpenHashSet<>();
							result = new ArrayList<>();
						}

						if (seen.add(shape)) {
							result.add(shape);
						}
					}
				}
			}
		}

		return result == null ? List.of() : result;
	}

	@Nullable
	public static AABB getBounds() {
		return bounds;
	}

	public static List<VoxelShape> feed(AABB box, List<VoxelShape> original) {
		fedThisMove = true;

		var shapes = shapes(box);

		if (shapes.isEmpty()) {
			return original;
		}

		var list = new ArrayList<VoxelShape>(original.size() + shapes.size());
		list.addAll(original);
		list.addAll(shapes);
		return list;
	}

	public static void beginMove() {
		fedThisMove = false;
	}

	public static Vec3 applyFallback(Entity entity, Vec3 movement, Vec3 result) {
		if (fedThisMove) {
			return result;
		}

		var shapes = shapes(entity.getBoundingBox().expandTowards(movement));

		if (shapes.isEmpty()) {
			return result;
		}

		var resolved = Vec3.ZERO;

		for (var axis : Direction.axisStepOrder(result)) {
			double axisMovement = result.get(axis);

			if (axisMovement != 0D) {
				double collision = Shapes.collide(axis, entity.getBoundingBox().move(resolved), shapes, axisMovement);
				resolved = resolved.with(axis, collision);
			}
		}

		return resolved;
	}

	public record Placement(
		StructureHolder holder,
		@Nullable AAIBB bounds,
		double x, double y, double z,
		float scaleX, float scaleY, float scaleZ,
		float rotationX, float rotationY, float rotationZ,
		double centerX, double centerY, double centerZ
	) {
		private void appendSignature(StringBuilder builder) {
			builder.append(System.identityHashCode(holder)).append(',')
				.append(bounds).append(',')
				.append(Double.doubleToLongBits(x)).append(',').append(Double.doubleToLongBits(y)).append(',').append(Double.doubleToLongBits(z)).append(',')
				.append(scaleX).append(',').append(scaleY).append(',').append(scaleZ).append(',')
				.append(rotationX).append(',').append(rotationY).append(',').append(rotationZ).append(',')
				.append(Double.doubleToLongBits(centerX)).append(',').append(Double.doubleToLongBits(centerY)).append(',').append(Double.doubleToLongBits(centerZ))
				.append(';');
		}
	}

	private record CollisionLevel(Long2ObjectMap<BlockState> blocks) implements BlockGetter {
		@Override
		@Nullable
		public BlockEntity getBlockEntity(BlockPos pos) {
			return null;
		}

		@Override
		public BlockState getBlockState(BlockPos pos) {
			return blocks.getOrDefault(pos.asLong(), AIR);
		}

		@Override
		public FluidState getFluidState(BlockPos pos) {
			var state = blocks.get(pos.asLong());
			return state == null ? EMPTY_FLUID : state.getFluidState();
		}

		@Override
		public int getHeight() {
			return 256;
		}

		@Override
		public int getMinY() {
			return 0;
		}
	}
}