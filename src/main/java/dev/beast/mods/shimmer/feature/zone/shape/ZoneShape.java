package dev.beast.mods.shimmer.feature.zone.shape;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.zone.Zone;
import dev.beast.mods.shimmer.feature.zone.ZoneClipResult;
import dev.beast.mods.shimmer.feature.zone.ZoneContainer;
import dev.beast.mods.shimmer.feature.zone.ZoneInstance;
import dev.beast.mods.shimmer.math.AAIBB;
import dev.beast.mods.shimmer.math.Line;
import dev.beast.mods.shimmer.util.registry.SimpleRegistry;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface ZoneShape {
	SimpleRegistry<ZoneShape> REGISTRY = SimpleRegistry.create(ZoneShape::type);
	Codec<ZoneShape> CODEC = Codec.either(AAIBB.CODEC, REGISTRY.valueCodec()).xmap(either -> either.map(box -> new BlockZoneShape(box.min(), box.max()), Function.identity()), shape -> shape instanceof BlockZoneShape b ? Either.left(b.toAAIBB()) : Either.right(shape));

	@AutoInit
	static void bootstrap() {
		REGISTRY.register(UniverseZoneShape.TYPE);
		REGISTRY.register(ZoneShapeGroup.TYPE);
		REGISTRY.register(BlockZoneShape.TYPE);
		REGISTRY.register(BoxZoneShape.TYPE);
		REGISTRY.register(SphereZoneShape.TYPE);
		REGISTRY.register(RotatedBoxZoneShape.TYPE);
	}

	default SimpleRegistryType<?> type() {
		return REGISTRY.getType(this);
	}

	default ZoneInstance createInstance(ZoneContainer container, Zone zone) {
		return new ZoneInstance(container, zone);
	}

	AABB getBoundingBox();

	default Vec3 getCenterPos() {
		var box = getBoundingBox();
		return new Vec3((box.minX + box.maxX) / 2D, (box.minY + box.maxY) / 2D, (box.minZ + box.maxZ) / 2D);
	}

	@Nullable
	default ZoneClipResult clip(ZoneInstance instance, Line ray) {
		if (contains(ray.start())) {
			return null;
		}

		var result = AABB.clip(List.of(getBoundingBox()), ray.start(), ray.end(), BlockPos.ZERO);

		if (result != null && result.getType() == HitResult.Type.BLOCK) {
			return ZoneClipResult.of(instance, this, ray, result);
		}

		return null;
	}

	default boolean contains(Vec3 pos) {
		return getBoundingBox().contains(pos);
	}

	default boolean intersects(AABB box) {
		return getBoundingBox().intersects(box);
	}

	default Stream<BlockPos> getBlocks() {
		return BlockPos.betweenClosedStream(getBoundingBox().inflate(0.5D)).filter(p -> contains(new Vec3(p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D)));
	}

	default List<Entity> collectEntities(Level level, Predicate<? super Entity> predicate) {
		return level.getEntities((Entity) null, getBoundingBox(), predicate);
	}

	default VoxelShape createVoxelShape() {
		return Shapes.create(getBoundingBox());
	}

	default VoxelShape createBlockRenderingShape(Predicate<BlockPos> predicate) {
		var voxelShape = new VoxelShape[1];

		getBlocks().filter(predicate).map(BlockPos::immutable).forEach(blockPos -> {
			double x = blockPos.getX();
			double y = blockPos.getY();
			double z = blockPos.getZ();
			var shape = Shapes.create(x, y, z, x + 1D, y + 1D, z + 1D);
			voxelShape[0] = voxelShape[0] == null ? shape : Shapes.or(voxelShape[0], shape);
		});

		return voxelShape[0] == null ? Shapes.empty() : voxelShape[0];
	}
}
