package dev.beast.mods.shimmer.feature.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.math.KMath;
import dev.beast.mods.shimmer.util.ShimmerStreamCodecs;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record SphereZoneShape(Vec3 pos, double radius, AABB box) implements ZoneShape {
	public static final ZoneShapeType<SphereZoneShape> TYPE = new ZoneShapeType<>("sphere", RecordCodecBuilder.mapCodec(instance -> instance.group(
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
	public ZoneShapeType<?> type() {
		return TYPE;
	}

	@Override
	public AABB getBoundingBox() {
		return box;
	}

	@Override
	public boolean contains(Vec3 pos) {
		return this.pos.distanceToSqr(pos) <= radius * radius;
	}

	@Override
	public boolean contains(AABB box) {
		double cx = (box.minX + box.maxX) / 2D;
		double cy = (box.minY + box.maxY) / 2D;
		double cz = (box.minZ + box.maxZ) / 2D;
		double sx = box.maxX - box.minX;
		double sy = box.maxY - box.minY;
		double sz = box.maxZ - box.minZ;

		double xd = Math.abs(pos.x() - cx);
		double yd = Math.abs(pos.y() - cy);
		double zd = Math.abs(pos.z() - cz);

		if (xd >= sx + radius || yd >= sy + radius || zd >= sz + radius) {
			return false;
		}

		if (xd < sx || yd < sy || zd < sz) {
			return true;
		}

		return KMath.sq(xd - sx) + KMath.sq(yd - sy) + KMath.sq(zd - sz) < radius * radius;
	}
}
