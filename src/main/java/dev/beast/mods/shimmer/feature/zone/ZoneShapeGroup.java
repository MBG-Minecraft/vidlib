package dev.beast.mods.shimmer.feature.zone;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public record ZoneShapeGroup(List<ZoneShape> zoneShapes, @Nullable AABB box) implements ZoneShape {
	public static final SimpleRegistryType<ZoneShapeGroup> TYPE = SimpleRegistryType.dynamic(Shimmer.id("group"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		ZoneShape.REGISTRY.valueCodec().listOf().fieldOf("zones").forGetter(ZoneShapeGroup::zoneShapes)
	).apply(instance, ZoneShapeGroup::create)), ZoneShape.REGISTRY.valueStreamCodec().apply(ByteBufCodecs.list()).map(ZoneShapeGroup::create, ZoneShapeGroup::zoneShapes));

	public static ZoneShapeGroup create(List<ZoneShape> zoneShapes) {
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double minZ = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		double maxZ = Double.NEGATIVE_INFINITY;

		for (var zone : zoneShapes) {
			if (zone.canMove()) {
				return new ZoneShapeGroup(zoneShapes, null);
			}

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
	public boolean canMove() {
		return box == null;
	}

	@Override
	public AABB getBoundingBox() {
		if (box != null) {
			return box;
		} else if (zoneShapes.size() == 1) {
			return zoneShapes.getFirst().getBoundingBox();
		}

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

		return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	@Nullable
	public ZoneClipResult clip(ZoneInstance instance, Vec3 start, Vec3 end) {
		ZoneClipResult result = null;

		for (var zone : zoneShapes) {
			var clip = zone.clip(instance, start, end);

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

		return stream.distinct();
	}
}
