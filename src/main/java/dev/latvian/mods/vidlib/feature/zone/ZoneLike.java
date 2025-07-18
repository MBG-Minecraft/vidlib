package dev.latvian.mods.vidlib.feature.zone;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3dc;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface ZoneLike {
	AABB getBoundingBox();

	default Vec3 getCenterPos() {
		var box = getBoundingBox();
		return new Vec3((box.minX + box.maxX) / 2D, (box.minY + box.maxY) / 2D, (box.minZ + box.maxZ) / 2D);
	}

	default boolean contains(double x, double y, double z) {
		return getBoundingBox().contains(x, y, z);
	}

	default boolean contains(Vec3 pos) {
		return contains(pos.x, pos.y, pos.z);
	}

	default boolean contains(Vec3i pos) {
		return contains(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
	}

	default boolean contains(Vector3dc pos) {
		return contains(pos.x(), pos.y(), pos.z());
	}

	default boolean intersects(AABB box) {
		return getBoundingBox().intersects(box);
	}

	default Stream<BlockPos> getBlocks() {
		return BlockPos.betweenClosedStream(getBoundingBox().inflate(0.5D)).filter(this::contains);
	}

	default List<Entity> collectEntities(Level level, Predicate<? super Entity> predicate) {
		return level.getEntities((Entity) null, getBoundingBox(), predicate);
	}

	default VoxelShape createVoxelShape() {
		return Shapes.create(getBoundingBox());
	}

	default VoxelShape createBlockRenderingShape(Predicate<BlockPos> predicate) {
		var voxelShape = new VoxelShape[1];

		getBlocks().filter(predicate).map(BlockPos::immutable).forEach(blockPos -> {
			double x = blockPos.getX();
			double y = blockPos.getY();
			double z = blockPos.getZ();
			var shape = Shapes.create(x, y, z, x + 1D, y + 1D, z + 1D);
			voxelShape[0] = voxelShape[0] == null ? shape : Shapes.or(voxelShape[0], shape);
		});

		return voxelShape[0] == null ? Shapes.empty() : voxelShape[0];
	}

	default double closestDistanceTo(Vec3 pos) {
		var box = getBoundingBox();
		return box.contains(pos) ? 0D : Math.sqrt(box.distanceToSqr(pos));
	}

	default void collectChunkPositions(LongSet chunks) {
		var box = getBoundingBox();
		int minX = (int) Math.floor(box.minX) >> 4;
		int minZ = (int) Math.floor(box.minZ) >> 4;
		int maxX = (int) Math.floor(box.maxX) >> 4;
		int maxZ = (int) Math.floor(box.maxZ) >> 4;

		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				chunks.add(ChunkPos.asLong(x, z));
			}
		}
	}

	default LongSet collectChunkPositions() {
		var chunks = new LongOpenHashSet();
		collectChunkPositions(chunks);
		return chunks;
	}
}
