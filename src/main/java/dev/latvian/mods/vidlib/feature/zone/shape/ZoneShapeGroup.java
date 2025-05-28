package dev.latvian.mods.vidlib.feature.zone.shape;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kmath.Line;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.feature.zone.ZoneClipResult;
import dev.latvian.mods.vidlib.feature.zone.ZoneInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public record ZoneShapeGroup(List<ZoneShape> zoneShapes, AABB box) implements ZoneShape {
	public static final SimpleRegistryType<ZoneShapeGroup> TYPE = SimpleRegistryType.dynamic("group", RecordCodecBuilder.mapCodec(instance -> instance.group(
		ZoneShape.CODEC.listOf().fieldOf("zones").forGetter(ZoneShapeGroup::zoneShapes)
	).apply(instance, ZoneShapeGroup::create)), ZoneShape.STREAM_CODEC.listOf().map(ZoneShapeGroup::create, ZoneShapeGroup::zoneShapes));

	public static ZoneShapeGroup create(List<ZoneShape> zoneShapes) {
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double minZ = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		double maxZ = Double.NEGATIVE_INFINITY;

		for (var zone : zoneShapes) {
			var box = zone.getBoundingBox();
			minX = Math.min(minX, box.minX);
			minY = Math.min(minY, box.minY);
			minZ = Math.min(minZ, box.minZ);
			maxX = Math.max(maxX, box.maxX);
			maxY = Math.max(maxY, box.maxY);
			maxZ = Math.max(maxZ, box.maxZ);
		}

		return new ZoneShapeGroup(zoneShapes, new AABB(minX, minY, minZ, maxX, maxY, maxZ));
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public AABB getBoundingBox() {
		return box;
	}

	@Override
	@Nullable
	public ZoneClipResult clip(ZoneInstance instance, Line ray) {
		ZoneClipResult result = null;

		for (var zone : zoneShapes) {
			var clip = zone.clip(instance, ray);

			if (clip != null) {
				if (result == null || clip.distanceSq() < result.distanceSq()) {
					result = clip;
				}
			}
		}

		return result;
	}

	@Override
	public boolean contains(Vec3 pos) {
		if (box != null && !box.contains(pos)) {
			return false;
		}

		for (var zone : zoneShapes) {
			if (zone.contains(pos)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean contains(Vec3i pos) {
		for (var zone : zoneShapes) {
			if (zone.contains(pos)) {
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
	public void writeUUID(FriendlyByteBuf buf) {
		buf.writeUtf(type().id());
		buf.writeVarInt(zoneShapes.size());

		for (var zone : zoneShapes) {
			zone.writeUUID(buf);
		}
	}
}
