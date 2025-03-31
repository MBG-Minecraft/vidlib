package dev.beast.mods.shimmer.math;

import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

import java.util.function.Consumer;
import java.util.stream.IntStream;

public record AAIBB(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
	public static final Codec<AAIBB> CODEC = Codec.INT_STREAM.comapFlatMap(r -> Util.fixedSize(r, 6).map(AAIBB::new), AAIBB::toIntStream).stable();

	public static final StreamCodec<ByteBuf, AAIBB> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, AAIBB::minX,
		ByteBufCodecs.VAR_INT, AAIBB::minY,
		ByteBufCodecs.VAR_INT, AAIBB::minZ,
		ByteBufCodecs.VAR_INT, AAIBB::maxX,
		ByteBufCodecs.VAR_INT, AAIBB::maxY,
		ByteBufCodecs.VAR_INT, AAIBB::maxZ,
		AAIBB::new
	);

	public AAIBB(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		this.minX = Math.min(minX, maxX);
		this.minY = Math.min(minY, maxY);
		this.minZ = Math.min(minZ, maxZ);
		this.maxX = Math.max(minX, maxX);
		this.maxY = Math.max(minY, maxY);
		this.maxZ = Math.max(minZ, maxZ);
	}

	public AAIBB(int[] array) {
		this(array[0], array[1], array[2], array[3], array[4], array[5]);
	}

	public AAIBB(Vec3i min, Vec3i max) {
		this(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
	}

	public BlockPos min() {
		return new BlockPos(minX, minY, minZ);
	}

	public BlockPos max() {
		return new BlockPos(maxX, maxY, maxZ);
	}

	public AABB aabb() {
		return new AABB(minX, minY, minZ, maxX + 1D, maxY + 1D, maxZ + 1D);
	}

	public int[] toIntArray() {
		return new int[]{minX, minY, minZ, maxX, maxY, maxZ};
	}

	public IntStream toIntStream() {
		return IntStream.of(toIntArray());
	}

	public void collectChunkPositions(LongSet chunks) {
		int cminX = minX >> 4;
		int cminZ = minZ >> 4;
		int cmaxX = maxX >> 4;
		int cmaxZ = maxZ >> 4;

		for (int x = cminX; x <= cmaxX; x++) {
			for (int z = cminZ; z <= cmaxZ; z++) {
				chunks.add(ChunkPos.asLong(x, z));
			}
		}
	}

	public LongSet collectChunkPositions() {
		var chunks = new LongOpenHashSet();
		collectChunkPositions(chunks);
		return chunks;
	}

	public boolean contains(int x, int y, int z) {
		return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
	}

	public boolean containsChunk(int x, int z) {
		int cminX = minX >> 4;
		int cminZ = minZ >> 4;
		int cmaxX = maxX >> 4;
		int cmaxZ = maxZ >> 4;
		return x >= cminX && x <= cmaxX && z >= cminZ && z <= cmaxZ;
	}

	public boolean containsChunk(ChunkPos pos) {
		return containsChunk(pos.x, pos.z);
	}

	public boolean containsSection(int x, int y, int z) {
		int cminX = minX >> 4;
		int cminY = minY >> 4;
		int cminZ = minZ >> 4;
		int cmaxX = maxX >> 4;
		int cmaxY = maxY >> 4;
		int cmaxZ = maxZ >> 4;
		return x >= cminX && x <= cmaxX && y >= cminY && y <= cmaxY && z >= cminZ && z <= cmaxZ;
	}

	public boolean containsSection(SectionPos pos) {
		return containsSection(pos.x(), pos.y(), pos.z());
	}

	public void forEveryEdgePosition(Consumer<BlockPos> consumer) {
		var pos = new BlockPos.MutableBlockPos();
		consumer.accept(pos.set(minX, minY, minZ));
		consumer.accept(pos.set(minX, minY, maxZ));
		consumer.accept(pos.set(minX, maxY, minZ));
		consumer.accept(pos.set(minX, maxY, maxZ));
		consumer.accept(pos.set(maxX, minY, minZ));
		consumer.accept(pos.set(maxX, minY, maxZ));
		consumer.accept(pos.set(maxX, maxY, minZ));
		consumer.accept(pos.set(maxX, maxY, maxZ));

		for (int x = minX + 1; x < maxX; x++) {
			consumer.accept(pos.set(x, minY, minZ));
			consumer.accept(pos.set(x, minY, maxZ));
			consumer.accept(pos.set(x, maxY, minZ));
			consumer.accept(pos.set(x, maxY, maxZ));
		}

		for (int y = minY + 1; y < maxY; y++) {
			consumer.accept(pos.set(minX, y, minZ));
			consumer.accept(pos.set(minX, y, maxZ));
			consumer.accept(pos.set(maxX, y, minZ));
			consumer.accept(pos.set(maxX, y, maxZ));
		}

		for (int z = minZ + 1; z < maxZ; z++) {
			consumer.accept(pos.set(minX, minY, z));
			consumer.accept(pos.set(minX, maxY, z));
			consumer.accept(pos.set(maxX, minY, z));
			consumer.accept(pos.set(maxX, maxY, z));
		}
	}
}
