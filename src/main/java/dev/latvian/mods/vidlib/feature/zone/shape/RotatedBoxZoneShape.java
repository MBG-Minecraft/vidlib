package dev.latvian.mods.vidlib.feature.zone.shape;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.JOMLCodecs;
import dev.latvian.mods.klib.codec.JOMLStreamCodecs;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.math.Line;
import dev.latvian.mods.klib.math.Rotation;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.feature.zone.ZoneClipResult;
import dev.latvian.mods.vidlib.feature.zone.ZoneInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Vector3f;

import java.util.List;

public record RotatedBoxZoneShape(Vec3 pos, Vector3f size, Rotation rotation, Matrix3f matrix, Matrix3f imatrix, AABB box, List<AABB> clipBox) implements ZoneShape {
	public static RotatedBoxZoneShape of(Vec3 pos, Vector3f size, Rotation rotation) {
		var matrix = new Matrix3f();
		rotation.rotateZXY(matrix);
		var imatrix = new Matrix3f(matrix).invert();

		var hsx = size.x() / 2F;
		var hsy = size.y() / 2F;
		var hsz = size.z() / 2F;

		var cnnn = new Vector3f(-hsx, -hsy, -hsz).mul(matrix);
		var cpnn = new Vector3f(+hsx, -hsy, -hsz).mul(matrix);
		var cnpn = new Vector3f(-hsx, +hsy, -hsz).mul(matrix);
		var cppn = new Vector3f(+hsx, +hsy, -hsz).mul(matrix);
		var cnnp = new Vector3f(-hsx, -hsy, +hsz).mul(matrix);
		var cpnp = new Vector3f(+hsx, -hsy, +hsz).mul(matrix);
		var cnpp = new Vector3f(-hsx, +hsy, +hsz).mul(matrix);
		var cppp = new Vector3f(+hsx, +hsy, +hsz).mul(matrix);

		var box = new AABB(
			pos.x() + KMath.min(cnnn.x, cpnn.x, cnpn.x, cppn.x, cnnp.x, cpnp.x, cnpp.x, cppp.x),
			pos.y() + KMath.min(cnnn.y, cpnn.y, cnpn.y, cppn.y, cnnp.y, cpnp.y, cnpp.y, cppp.y),
			pos.z() + KMath.min(cnnn.z, cpnn.z, cnpn.z, cppn.z, cnnp.z, cpnp.z, cnpp.z, cppp.z),
			pos.x() + KMath.max(cnnn.x, cpnn.x, cnpn.x, cppn.x, cnnp.x, cpnp.x, cnpp.x, cppp.x),
			pos.y() + KMath.max(cnnn.y, cpnn.y, cnpn.y, cppn.y, cnnp.y, cpnp.y, cnpp.y, cppp.y),
			pos.z() + KMath.max(cnnn.z, cpnn.z, cnpn.z, cppn.z, cnnp.z, cpnp.z, cnpp.z, cppp.z)
		);

		return new RotatedBoxZoneShape(pos, size, rotation, matrix, imatrix, box, List.of(new AABB(-hsx, -hsy, -hsz, hsx, hsy, hsz)));
	}

	public static final SimpleRegistryType<RotatedBoxZoneShape> TYPE = SimpleRegistryType.dynamic("rotated_box", RecordCodecBuilder.mapCodec(instance -> instance.group(
		MCCodecs.VEC3.fieldOf("pos").forGetter(RotatedBoxZoneShape::pos),
		JOMLCodecs.VEC3S.fieldOf("size").forGetter(RotatedBoxZoneShape::size),
		Rotation.CODEC.fieldOf("rotation").forGetter(RotatedBoxZoneShape::rotation)
	).apply(instance, RotatedBoxZoneShape::of)), CompositeStreamCodec.of(
		MCStreamCodecs.VEC3, RotatedBoxZoneShape::pos,
		JOMLStreamCodecs.VEC3S, RotatedBoxZoneShape::size,
		Rotation.STREAM_CODEC, RotatedBoxZoneShape::rotation,
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

		var rstart = new Vector3f((float) (ray.start().x - pos.x), (float) (ray.start().y - pos.y), (float) (ray.start().z - pos.z)).mul(matrix);
		var rend = new Vector3f((float) (ray.end().x - pos.x), (float) (ray.end().y - pos.y), (float) (ray.end().z - pos.z)).mul(matrix);

		var result = AABB.clip(clipBox, new Vec3(rstart.x, rstart.y, rstart.z), new Vec3(rend.x, rend.y, rend.z), BlockPos.ZERO);

		if (result != null && result.getType() == HitResult.Type.BLOCK) {
			var l = result.getLocation();
			var vec = new Vector3f((float) l.x, (float) l.y, (float) l.z).mul(imatrix);
			var apos = new Vec3(vec.x + pos.x, vec.y + pos.y, vec.z + pos.z);
			return ZoneClipResult.of(instance, this, ray, apos);
		}

		return null;
	}

	@Override
	public boolean contains(double x, double y, double z) {
		if (box.contains(x, y, z)) {
			var vec = new Vector3f((float) (x - pos.x), (float) (y - pos.y), (float) (z - pos.z)).mul(matrix);
			var hsx = size.x() / 2F;
			var hsy = size.y() / 2F;
			var hsz = size.z() / 2F;
			return vec.x >= -hsx && vec.x <= hsx && vec.y >= -hsy && vec.y <= hsy && vec.z >= -hsz && vec.z <= hsz;
		}

		return false;
	}

	@Override
	public boolean intersects(AABB box) {
		double cx = Math.clamp(pos.x, box.minX, box.maxX);
		double cy = Math.clamp(pos.y, box.minY, box.maxY);
		double cz = Math.clamp(pos.z, box.minZ, box.maxZ);

		var hsx = size.x() / 2F;
		var hsy = size.y() / 2F;
		var hsz = size.z() / 2F;

		var vec = new Vector3f((float) (cx - pos.x), (float) (cy - pos.y), (float) (cz - pos.z)).mul(matrix);
		return vec.x >= -hsx && vec.x <= hsx && vec.y >= -hsy && vec.y <= hsy && vec.z >= -hsz && vec.z <= hsz;
	}

	@Override
	public ZoneShape move(double x, double y, double z) {
		return of(pos.add(x, y, z), size, rotation);
	}

	@Override
	public ZoneShape scale(double x, double y, double z) {
		return of(pos, size.mul((float) x, (float) y, (float) z, new Vector3f()), rotation);
	}
}
