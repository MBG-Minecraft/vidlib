package dev.beast.mods.shimmer.feature.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public record SphereZoneShape(Vec3 pos, double radius, AABB box) implements ZoneShape {
	public static final SimpleRegistryType<SphereZoneShape> TYPE = SimpleRegistryType.dynamic(Shimmer.id("sphere"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		Vec3.CODEC.fieldOf("pos").forGetter(SphereZoneShape::pos),
		Codec.doubleRange(0D, Double.POSITIVE_INFINITY).fieldOf("radius").forGetter(SphereZoneShape::radius)
	).apply(instance, SphereZoneShape::new)), StreamCodec.composite(
		ShimmerStreamCodecs.VEC_3,
		SphereZoneShape::pos,
		ByteBufCodecs.DOUBLE,
		SphereZoneShape::radius,
		SphereZoneShape::new
	));

	public SphereZoneShape(Vec3 pos, double radius) {
		this(pos, radius, new AABB(pos.x() - radius, pos.y() - radius, pos.z() - radius, pos.x() + radius, pos.y() + radius, pos.z() + radius));
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
	public ZoneClipResult clip(ZoneInstance instance, Vec3 start, Vec3 end) {
		double dx = end.x - start.x;
		double dy = end.y - start.y;
		double dz = end.z - start.z;

		double fx = start.x - pos.x;
		double fy = start.y - pos.y;
		double fz = start.z - pos.z;

		double a = dx * dx + dy * dy + dz * dz;
		double b = 2 * (fx * dx + fy * dy + fz * dz);
		double c = (fx * fx + fy * fy + fz * fz) - (radius * radius);

		double discriminant = b * b - 4 * a * c;

		if (discriminant < 0) {
			return null;
		}

		// Compute the two possible intersection points
		double t1 = (-b - Math.sqrt(discriminant)) / (2 * a);
		double t2 = (-b + Math.sqrt(discriminant)) / (2 * a);

		// Check if either intersection is within the segment range [0, 1]
		if (t1 >= 0 && t1 <= 1 || t2 >= 0 && t2 <= 1) {
			// FIXME: Get actual intersection point
			return new ZoneClipResult(instance, this, pos.distanceToSqr(start), pos, null);
		}

		return null;
	}

	@Override
	public boolean contains(Vec3 pos) {
		return this.pos.distanceToSqr(pos) <= radius * radius;
	}

	@Override
	public boolean intersects(AABB box) {
		double dx = Math.abs(pos.x() - Math.clamp(pos.x, box.minX, box.maxX));
		double dy = Math.abs(pos.y() - Math.clamp(pos.y, box.minY, box.maxY));
		double dz = Math.abs(pos.z() - Math.clamp(pos.z, box.minZ, box.maxZ));
		return dx * dx + dy * dy + dz * dz < radius * radius;
	}

	@Override
	public Stream<BlockPos> getBlocks() {
		return BlockPos.betweenClosedStream(box).filter(p -> pos.distanceToSqr(p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D) <= radius * radius);
	}
}
