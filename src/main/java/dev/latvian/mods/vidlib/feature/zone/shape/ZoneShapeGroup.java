package dev.latvian.mods.vidlib.feature.zone.shape;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.math.AAIBB;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.feature.zone.ZoneClipResult;
import dev.latvian.mods.vidlib.feature.zone.ZoneInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public record ZoneShapeGroup(List<ZoneShape> zoneShapes, AABB box, AAIBB intBox) implements ZoneShape {
	public static final SimpleRegistryType<ZoneShapeGroup> TYPE = SimpleRegistryType.dynamic("group", RecordCodecBuilder.mapCodec(instance -> instance.group(
		ZoneShape.CODEC.listOf().fieldOf("zones").forGetter(ZoneShapeGroup::zoneShapes)
	).apply(instance, ZoneShapeGroup::create)), KLibStreamCodecs.listOf(ZoneShape.STREAM_CODEC).map(ZoneShapeGroup::create, ZoneShapeGroup::zoneShapes));

	public static ZoneShapeGroup create(List<ZoneShape> zoneShapes) {
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double minZ = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		double maxZ = Double.NEGATIVE_INFINITY;
		int iminX = Integer.MAX_VALUE;
		int iminY = Integer.MAX_VALUE;
		int iminZ = Integer.MAX_VALUE;
		int imaxX = Integer.MIN_VALUE;
		int imaxY = Integer.MIN_VALUE;
		int imaxZ = Integer.MIN_VALUE;

		for (var zone : zoneShapes) {
			var box = zone.toAABB();
			minX = Math.min(minX, box.minX);
			minY = Math.min(minY, box.minY);
			minZ = Math.min(minZ, box.minZ);
			maxX = Math.max(maxX, box.maxX);
			maxY = Math.max(maxY, box.maxY);
			maxZ = Math.max(maxZ, box.maxZ);
			var ibox = zone.toAAIBB();
			iminX = Math.min(iminX, ibox.minX());
			iminY = Math.min(iminY, ibox.minY());
			iminZ = Math.min(iminZ, ibox.minZ());
			imaxX = Math.max(imaxX, ibox.maxX());
			imaxY = Math.max(imaxY, ibox.maxY());
			imaxZ = Math.max(imaxZ, ibox.maxZ());
		}

		return new ZoneShapeGroup(zoneShapes, new AABB(minX, minY, minZ, maxX, maxY, maxZ), new AAIBB(iminX, iminY, iminZ, imaxX, imaxY, imaxZ));
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public AABB toAABB() {
		return box;
	}

	@Override
	@Nullable
	public ZoneClipResult clip(ZoneInstance instance, ClipContext ctx) {
		ZoneClipResult result = null;

		for (var zone : zoneShapes) {
			var clip = zone.clip(instance, ctx);

			if (clip != null) {
				if (result == null || clip.distanceSq() < result.distanceSq()) {
					result = clip;
				}
			}
		}

		return result;
	}

	@Override
	public boolean contains(double x, double y, double z) {
		if (box != null && !box.contains(x, y, z)) {
			return false;
		}

		for (var zone : zoneShapes) {
			if (zone.contains(x, y, z)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean contains(int x, int y, int z) {
		if (intBox != null && !intBox.contains(x, y, z)) {
			return false;
		}

		for (var zone : zoneShapes) {
			if (zone.contains(x, y, z)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean intersects(AABB box) {
		if (box != null && !box.intersects(box)) {
			return false;
		}

		for (var zone : zoneShapes) {
			if (zone.intersects(box)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public Stream<BlockPos> getBlocks() {
		var stream = Stream.<BlockPos>empty();

		for (var zone : zoneShapes) {
			stream = Stream.concat(stream, zone.getBlocks());
		}

		return stream;
	}

	@Override
	public VoxelShape createVoxelShape() {
		if (zoneShapes.isEmpty()) {
			return Shapes.empty();
		}

		var shape = zoneShapes.getFirst().createVoxelShape();

		for (int i = 1; i < zoneShapes.size(); i++) {
			shape = Shapes.or(shape, zoneShapes.get(i).createVoxelShape());
		}

		return shape;
	}

	@Override
	public VoxelShape createBlockRenderingShape(Predicate<BlockPos> predicate) {
		if (zoneShapes.isEmpty()) {
			return Shapes.empty();
		}

		var shape = zoneShapes.getFirst().createBlockRenderingShape(predicate);

		for (int i = 1; i < zoneShapes.size(); i++) {
			shape = Shapes.or(shape, zoneShapes.get(i).createBlockRenderingShape(predicate));
		}

		return shape;
	}

	@Override
	public double closestDistanceTo(Vec3 pos) {
		var dist = Double.POSITIVE_INFINITY;

		for (var shape : zoneShapes) {
			dist = Math.min(dist, shape.closestDistanceTo(pos));

			if (dist <= 0D) {
				return 0D;
			}
		}

		return dist;
	}

	@Override
	public ZoneShape move(double x, double y, double z) {
		var shapes = new ArrayList<ZoneShape>(zoneShapes.size());

		for (var shape : zoneShapes) {
			shapes.add(shape.move(x, y, z));
		}

		var moved = box.move(x, y, z);
		return new ZoneShapeGroup(shapes, moved, new AAIBB(Mth.floor(moved.minX), Mth.floor(moved.minY), Mth.floor(moved.minZ), Mth.ceil(moved.maxX), Mth.ceil(moved.maxY), Mth.ceil(moved.maxZ)));
	}

	@Override
	public ZoneShape scale(double x, double y, double z) {
		return this;
	}
}
