package dev.beast.mods.shimmer.feature.zone;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ZoneGroup(List<Zone> zones, @Nullable AABB box) implements Zone {
	public static final ZoneType<ZoneGroup> TYPE = new ZoneType<>("group", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Zone.CODEC.listOf().fieldOf("zones").forGetter(ZoneGroup::zones)
	).apply(instance, ZoneGroup::create)), Zone.STREAM_CODEC.apply(ByteBufCodecs.list()).map(ZoneGroup::create, ZoneGroup::zones));

	public static ZoneGroup create(List<Zone> zones) {
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double minZ = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		double maxZ = Double.NEGATIVE_INFINITY;

		for (var zone : zones) {
			if (zone.canMove()) {
				return new ZoneGroup(zones, null);
			}

			var box = zone.getBoundingBox();
			minX = Math.min(minX, box.minX);
			minY = Math.min(minY, box.minY);
			minZ = Math.min(minZ, box.minZ);
			maxX = Math.max(maxX, box.maxX);
			maxY = Math.max(maxY, box.maxY);
			maxZ = Math.max(maxZ, box.maxZ);
		}

		return new ZoneGroup(zones, new AABB(minX, minY, minZ, maxX, maxY, maxZ));
	}

	@Override
	public ZoneType<?> type() {
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
		} else if (zones.size() == 1) {
			return zones.getFirst().getBoundingBox();
		}

		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double minZ = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		double maxZ = Double.NEGATIVE_INFINITY;

		for (var zone : zones) {
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
	public ZoneClipResult clip(Vec3 start, Vec3 end) {
		ZoneClipResult result = null;

		for (var zone : zones) {
			var clip = zone.clip(start, end);

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

		for (var zone : zones) {
			if (zone.contains(pos)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean contains(AABB box) {
		if (box != null && !box.intersects(box)) {
			return false;
		}

		for (var zone : zones) {
			if (zone.contains(box)) {
				return true;
			}
		}

		return false;
	}
}
