package dev.latvian.mods.vidlib.feature.zone.shape;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.shape.CylinderShape;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.feature.zone.ZoneClipResult;
import dev.latvian.mods.vidlib.feature.zone.ZoneInstance;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

public record CylinderZoneShape(Vec3 pos, CylinderShape shape, AABB box) implements ZoneShape {
	public static final SimpleRegistryType<CylinderZoneShape> TYPE = SimpleRegistryType.dynamic("cylinder", RecordCodecBuilder.mapCodec(instance -> instance.group(
		MCCodecs.VEC3.fieldOf("pos").forGetter(CylinderZoneShape::pos),
		CylinderShape.CODEC.forGetter(CylinderZoneShape::shape)
	).apply(instance, CylinderZoneShape::new)), CompositeStreamCodec.of(
		MCStreamCodecs.VEC3, CylinderZoneShape::pos,
		CylinderShape.STREAM_CODEC, CylinderZoneShape::shape,
		CylinderZoneShape::new
	));

	public CylinderZoneShape(Vec3 pos, CylinderShape shape) {
		this(pos, shape, new AABB(pos.x() - shape.radius(), pos.y() - shape.height() / 2D, pos.z() - shape.radius(), pos.x() + shape.radius(), pos.y() + shape.height() / 2D, pos.z() + shape.radius()));
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
	public ZoneClipResult clip(ZoneInstance instance, ClipContext ctx) {
		return ZoneShape.super.clip(instance, ctx); // FIXME
	}

	@Override
	public boolean contains(double x, double y, double z) {
		return Math.abs(pos.y - y) <= shape.height() / 2D && Vector2d.distanceSquared(pos.x, pos.z, x, z) <= shape.radius() * shape.radius();
	}

	@Override
	public boolean intersects(AABB box) {
		double dx = pos.x() - Math.clamp(pos.x, box.minX, box.maxX);
		double dy = pos.y() - Math.clamp(pos.y, box.minY, box.maxY);
		double dz = pos.z() - Math.clamp(pos.z, box.minZ, box.maxZ);
		return dx * dx + dz * dz <= shape.radius() * shape.radius() && Math.abs(dy) <= shape.height() / 2D;
	}

	@Override
	public VoxelShape createVoxelShape() {
		return ZoneShape.super.createVoxelShape();
	}

	@Override
	public double closestDistanceTo(Vec3 pos) {
		return Math.max(0D, this.pos.distanceTo(pos) - shape.radius());
	}

	@Override
	public ZoneShape move(double x, double y, double z) {
		return new CylinderZoneShape(pos.add(x, y, z), shape, box.move(x, y, z));
	}

	@Override
	public ZoneShape scale(double x, double y, double z) {
		return new CylinderZoneShape(pos, new CylinderShape((float) (shape.radius() * x * z), (float) (shape.height() * y)));
	}
}
