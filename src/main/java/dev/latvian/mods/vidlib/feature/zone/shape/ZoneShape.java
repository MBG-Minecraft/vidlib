package dev.latvian.mods.vidlib.feature.zone.shape;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kmath.AAIBB;
import dev.latvian.mods.kmath.Line;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.feature.zone.Zone;
import dev.latvian.mods.vidlib.feature.zone.ZoneClipResult;
import dev.latvian.mods.vidlib.feature.zone.ZoneContainer;
import dev.latvian.mods.vidlib.feature.zone.ZoneInstance;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
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
	StreamCodec<RegistryFriendlyByteBuf, ZoneShape> STREAM_CODEC = ByteBufCodecs.either(AAIBB.STREAM_CODEC, REGISTRY.valueStreamCodec()).map(either -> either.map(box -> new BlockZoneShape(box.min(), box.max()), Function.identity()), shape -> shape instanceof BlockZoneShape b ? Either.left(b.toAAIBB()) : Either.right(shape));

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

	default boolean contains(BlockPos pos) {
		return contains(Vec3.atCenterOf(pos));
	}

	default boolean intersects(AABB box) {
		return getBoundingBox().intersects(box);
	}

	default Stream<BlockPos> getBlocks() {
		return BlockPos.betweenClosedStream(getBoundingBox().inflate(0.5D)).filter(this::contains);
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

	default double closestDistanceTo(Vec3 pos) {
		var box = getBoundingBox();

		if (box.contains(pos)) {
			return 0D;
		}

		double dx = pos.x() - Math.clamp(pos.x, box.minX, box.maxX);
		double dy = pos.y() - Math.clamp(pos.y, box.minY, box.maxY);
		double dz = pos.z() - Math.clamp(pos.z, box.minZ, box.maxZ);
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	default void writeUUID(FriendlyByteBuf buf) {
		buf.writeUtf(type().id().toString());
		buf.writeUtf(toString());
	}

	default void collectChunkPositions(LongSet chunks) {
		var box = getBoundingBox();
		int minX = (int) Math.floor(box.minX) >> 4;
		int minZ = (int) Math.floor(box.minZ) >> 4;
		int maxX = (int) Math.floor(box.maxX) >> 4;
		int maxZ = (int) Math.floor(box.maxZ) >> 4;

		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				chunks.add(ChunkPos.asLong(x, z));
			}
		}
	}

	default LongSet collectChunkPositions() {
		var chunks = new LongOpenHashSet();
		collectChunkPositions(chunks);
		return chunks;
	}
}
