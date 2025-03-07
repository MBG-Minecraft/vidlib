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
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3d;
import org.joml.Vector3d;

import java.util.stream.Stream;

public record RotatedBoxZoneShape(Vec3 pos, Vec3 size, double rotation, Matrix3d matrix, AABB box) implements ZoneShape {
	public static RotatedBoxZoneShape of(Vec3 pos, Vec3 size, double rotation) {
		var matrix = new Matrix3d().rotateY(Math.toRadians(rotation));

		var hsx = size.x() / 2D;
		var hsy = size.y() / 2D;
		var hsz = size.z() / 2D;

		var cnn = new Vector3d(-hsx, 0D, -hsz).mul(matrix);
		var cpn = new Vector3d(+hsx, 0D, -hsz).mul(matrix);
		var cnp = new Vector3d(-hsx, 0D, +hsz).mul(matrix);
		var cpp = new Vector3d(+hsx, 0D, +hsz).mul(matrix);

		var minX = Math.min(Math.min(Math.min(cnn.x, cpn.x), cnp.x), cpp.x);
		var minZ = Math.min(Math.min(Math.min(cnn.z, cpn.z), cnp.z), cpp.z);
		var maxX = Math.max(Math.max(Math.max(cnn.x, cpn.x), cnp.x), cpp.x);
		var maxZ = Math.max(Math.max(Math.max(cnn.z, cpn.z), cnp.z), cpp.z);

		var box = new AABB(
			pos.x() + minX,
			pos.y() - hsy,
			pos.z() + minZ,
			pos.x() + maxX,
			pos.y() + hsy,
			pos.z() + maxZ
		);

		return new RotatedBoxZoneShape(pos, size, rotation, matrix, box);
	}

	public static final SimpleRegistryType<RotatedBoxZoneShape> TYPE = SimpleRegistryType.dynamic(Shimmer.id("rotated_box"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		ShimmerCodecs.VEC_3D.fieldOf("pos").forGetter(RotatedBoxZoneShape::pos),
		ShimmerCodecs.VEC_3D.fieldOf("size").forGetter(RotatedBoxZoneShape::size),
		Codec.DOUBLE.fieldOf("rotation").forGetter(RotatedBoxZoneShape::rotation)
	).apply(instance, RotatedBoxZoneShape::of)), CompositeStreamCodec.of(
		ShimmerStreamCodecs.VEC_3,
		RotatedBoxZoneShape::pos,
		ShimmerStreamCodecs.VEC_3,
		RotatedBoxZoneShape::size,
		ByteBufCodecs.DOUBLE,
		RotatedBoxZoneShape::rotation,
		RotatedBoxZoneShape::of
	));

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
		if (contains(ray.start())) {
			return null;
		}

		return null;
	}

	@Override
	public boolean contains(Vec3 p) {
		var dy = p.y - pos.y;
		var hsy = size.y() / 2D;

		if (dy < -hsy || dy > hsy) {
			return false;
		}

		var hsx = size.x() / 2D;
		var hsz = size.z() / 2D;
		var vec = new Vector3d(p.x - pos.x, 0D, p.z - pos.z).mul(matrix);
		return vec.x >= -hsx && vec.x <= hsx && vec.z >= -hsz && vec.z <= hsz;
	}

	@Override
	public boolean intersects(AABB box) {
		var hsy = size.y() / 2D;

		if (pos.y + hsy < box.minY || pos.y - hsy > box.maxY) {
			return false;
		}

		double cx = Math.clamp(pos.x, box.minX, box.maxX);
		double cz = Math.clamp(pos.z, box.minZ, box.maxZ);

		var hsx = size.x() / 2D;
		var hsz = size.z() / 2D;
		var vec = new Vector3d(cx - pos.x, 0D, cz - pos.z).mul(matrix);
		return vec.x >= -hsx && vec.x <= hsx && vec.z >= -hsz && vec.z <= hsz;
	}

	@Override
	public Stream<BlockPos> getBlocks() {
		return BlockPos.betweenClosedStream(box.inflate(0.5D)).filter(p -> contains(new Vec3(p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D)));
	}
}
