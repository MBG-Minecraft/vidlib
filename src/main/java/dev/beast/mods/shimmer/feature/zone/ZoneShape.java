package dev.beast.mods.shimmer.feature.zone;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.math.AAIBB;
import dev.beast.mods.shimmer.util.registry.SimpleRegistry;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface ZoneShape {
	SimpleRegistry<ZoneShape> REGISTRY = SimpleRegistry.create(ZoneShape::type);
	Codec<ZoneShape> CODEC = Codec.either(AAIBB.CODEC, REGISTRY.valueCodec()).xmap(either -> either.map(box -> new BlockZoneShape(box.min(), box.max()), Function.identity()), shape -> shape instanceof BlockZoneShape b ? Either.left(b.toAAIBB()) : Either.right(shape));

	static void bootstrap() {
		REGISTRY.register(UniverseZoneShape.TYPE);
		REGISTRY.register(ZoneShapeGroup.TYPE);
		REGISTRY.register(BlockZoneShape.TYPE);
		REGISTRY.register(BoxZoneShape.TYPE);
		REGISTRY.register(SphereZoneShape.TYPE);
	}

	default SimpleRegistryType<?> type() {
		return REGISTRY.getType(this);
	}

	default ZoneInstance createInstance(ZoneContainer container, Zone zone) {
		return new ZoneInstance(container, zone);
	}

	default boolean canMove() {
		return false;
	}

	AABB getBoundingBox();

	@Nullable
	default ZoneClipResult clip(ZoneInstance instance, Vec3 start, Vec3 end) {
		if (contains(start)) {
			return null;
		}

		var result = AABB.clip(List.of(getBoundingBox()), start, end, BlockPos.ZERO);

		if (result != null && result.getType() == HitResult.Type.BLOCK) {
			var pos = result.getLocation();
			return new ZoneClipResult(instance, this, pos.distanceToSqr(start), pos, result);
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
		return BlockPos.betweenClosedStream(getBoundingBox());
	}

	default List<Entity> collectEntities(Level level, Predicate<? super Entity> predicate) {
		return level.getEntities((Entity) null, getBoundingBox(), predicate);
	}
}
