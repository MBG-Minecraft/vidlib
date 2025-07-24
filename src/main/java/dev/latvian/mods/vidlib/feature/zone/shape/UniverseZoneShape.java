package dev.latvian.mods.vidlib.feature.zone.shape;

import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.feature.zone.ZoneClipResult;
import dev.latvian.mods.vidlib.feature.zone.ZoneInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.stream.Stream;

public class UniverseZoneShape implements ZoneShape {
	public static final SimpleRegistryType<UniverseZoneShape> TYPE = SimpleRegistryType.unit("universe", new UniverseZoneShape());

	private UniverseZoneShape() {
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public AABB getBoundingBox() {
		return AABB.INFINITE;
	}

	@Override
	public Vec3 getCenterPos() {
		return Vec3.ZERO;
	}

	@Override
	@Nullable
	public ZoneClipResult clip(ZoneInstance instance, ClipContext ctx) {
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
