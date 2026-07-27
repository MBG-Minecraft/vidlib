package dev.latvian.mods.vidlib.feature.zone.shape;

import dev.latvian.mods.klib.registry.UnitType;
import dev.latvian.mods.vidlib.feature.zone.ZoneClipContext;
import dev.latvian.mods.vidlib.feature.zone.ZoneClipResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.stream.Stream;

public enum UniverseZoneShape implements ZoneShape {
	INSTANCE;

	public static final UnitType<ByteBuf, ZoneShape> TYPE = UnitType.create("universe", INSTANCE);

	@Override
	public UnitType<ByteBuf, ZoneShape> type() {
		return TYPE;
	}

	@Override
	public AABB toAABB() {
		return AABB.INFINITE;
	}

	@Override
	public Vec3 getCenterPos() {
		return Vec3.ZERO;
	}

	@Override
	@Nullable
	public ZoneClipResult clip(ZoneClipContext ctx) {
		return null;
	}

	@Override
	public boolean contains(double x, double y, double z) {
		return true;
	}

	@Override
	public boolean intersects(AABB box) {
		return true;
	}

	@Override
	public Stream<BlockPos> getBlocks() {
		return Stream.empty();
	}

	@Override
	public VoxelShape createVoxelShape() {
		return Shapes.empty();
	}

	@Override
	public VoxelShape createBlockRenderingShape(Predicate<BlockPos> predicate) {
		return Shapes.empty();
	}

	@Override
	public double closestDistanceTo(Vec3 pos) {
		return Double.POSITIVE_INFINITY;
	}

	@Override
	public ZoneShape move(double x, double y, double z) {
		return this;
	}

	@Override
	public ZoneShape scale(double x, double y, double z) {
		return this;
	}
}
