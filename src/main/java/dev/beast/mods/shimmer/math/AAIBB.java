package dev.beast.mods.shimmer.math;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;

import java.util.stream.IntStream;

public record AAIBB(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
	public static final Codec<AAIBB> CODEC = Codec.INT_STREAM.comapFlatMap(r -> Util.fixedSize(r, 6).map(AAIBB::new), AAIBB::toIntStream).stable();

	public static final StreamCodec<ByteBuf, AAIBB> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		AAIBB::minX,
		ByteBufCodecs.VAR_INT,
		AAIBB::minY,
		ByteBufCodecs.VAR_INT,
		AAIBB::minZ,
		ByteBufCodecs.VAR_INT,
		AAIBB::maxX,
		ByteBufCodecs.VAR_INT,
		AAIBB::maxY,
		ByteBufCodecs.VAR_INT,
		AAIBB::maxZ,
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
}
