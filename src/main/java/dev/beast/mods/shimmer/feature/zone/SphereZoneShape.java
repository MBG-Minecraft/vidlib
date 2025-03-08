package dev.beast.mods.shimmer.feature.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.math.Line;
import dev.beast.mods.shimmer.util.CompositeStreamCodec;
import dev.beast.mods.shimmer.util.ShimmerCodecs;
import dev.beast.mods.shimmer.util.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public record SphereZoneShape(Vec3 pos, double radius, AABB box) implements ZoneShape {
	public static final SimpleRegistryType<SphereZoneShape> TYPE = SimpleRegistryType.dynamic(Shimmer.id("sphere"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		ShimmerCodecs.VEC_3D.fieldOf("pos").forGetter(SphereZoneShape::pos),
		Codec.doubleRange(0D, Double.POSITIVE_INFINITY).fieldOf("radius").forGetter(SphereZoneShape::radius)
	).apply(instance, SphereZoneShape::new)), CompositeStreamCodec.of(
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
	public Vec3 getCenterPos() {
		return pos;
	}

	@Override
	@Nullable
	public ZoneClipResult clip(ZoneInstance instance, Line ray) {
		double dx = ray.dx();
		double dy = ray.dy();
		double dz = ray.dz();

		double fx = ray.start().x - pos.x;
		double fy = ray.start().y - pos.y;
		double fz = ray.start().z - pos.z;

		double a = dx * dx + dy * dy + dz * dz;
		double b = 2D * (fx * dx + fy * dy + fz * dz);
		double c = (fx * fx + fy * fy + fz * fz) - (radius * radius);

		double discriminant = b * b - 4D * a * c;

		if (discriminant < 0D) {
			return null;
		}

		var dsq = Math.sqrt(discriminant);
		// Compute the two possible intersection points
		double t1 = (-b - dsq) / (2D * a);
		double t2 = (-b + dsq) / (2D * a);

		// Check if either intersection is within the segment range [0, 1]
		if (t1 >= 0D && t1 <= 1D || t2 >= 0D && t2 <= 1D) {
			// FIXME: Get actual intersection point
			return ZoneClipResult.of(instance, this, ray, pos);
		}

		return null;
	}

	@Override
	public boolean contains(Vec3 pos) {
		return this.pos.distanceToSqr(pos) <= radius * radius;
	}

	@Override
	public boolean intersects(AABB box) {
		double dx = pos.x() - Math.clamp(pos.x, box.minX, box.maxX);
		double dy = pos.y() - Math.clamp(pos.y, box.minY, box.maxY);
		double dz = pos.z() - Math.clamp(pos.z, box.minZ, box.maxZ);
		return dx * dx + dy * dy + dz * dz < radius * radius;
	}

	@Override
	public Stream<BlockPos> getBlocks() {
		return BlockPos.betweenClosedStream(box).filter(p -> pos.distanceToSqr(p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D) <= radius * radius);
	}

	@Override
	public VoxelShape createVoxelShape() {
		return ZoneShape.super.createVoxelShape();

		/*
		VoxelShape shape = Shapes.empty();

		double sqrt2 = Math.sqrt(2D);

		double slice = 0.125D;
		int slices = Mth.floor(radius / slice);

		for (int i = 1; i < slices; i++) {
			double y0 = (i - 1) / (double) slices;
			double y1 = i / (double) slices;
			double r0 = Math.cos(y0 / radius * Math.PI / 2D) * radius;
			double r1 = Math.cos(y1 / radius * Math.PI / 2D) * radius;

			shape = Shapes.or(shape, Shapes.create(pos.x - r0 / sqrt2, pos.y + y0 * slice, pos.z - r0, pos.x + r1 / sqrt2, pos.y + y1 * slice, pos.z + r1));
			// shape = Shapes.or(shape, Shapes.create(pos.x - r0, pos.y + y0 * slice, pos.z - r0 / sqrt2, pos.x + r1, pos.y + y1 * slice, pos.z + r1 / sqrt2));
		}

		return shape == null ? Shapes.empty() : shape;
		 */
	}
}
