package dev.beast.mods.shimmer.math;

import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record UV(float u0, float v0, float u1, float v1) {
	public static final UV FULL = new UV(0F, 0F, 1F, 1F);

	public static final Codec<UV> CODEC = Codec.FLOAT.listOf(4, 4).xmap(f -> f.get(0) == 0F && f.get(1) == 0F && f.get(2) == 2F && f.get(3) == 1F ? FULL : new UV(f.get(0), f.get(1), f.get(2), f.get(3)), uv -> List.of(uv.u0, uv.v0, uv.u1, uv.v1));

	public static final StreamCodec<ByteBuf, UV> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.FLOAT, UV::u0,
		ByteBufCodecs.FLOAT, UV::v0,
		ByteBufCodecs.FLOAT, UV::u1,
		ByteBufCodecs.FLOAT, UV::v1,
		UV::new
	);

	public UV uvsByFace(Vec3f from, Vec3f to, Direction face) {
		return switch (face) {
			case DOWN -> new UV(from.x(), 1F - to.z(), to.x(), 1F - from.z());
			case UP -> new UV(from.x(), from.z(), to.x(), to.z());
			case NORTH -> new UV(1F - to.x(), 1F - to.y(), 1F - from.x(), 1F - from.y());
			case SOUTH -> new UV(from.x(), 1F - to.y(), to.x(), 1F - from.y());
			case WEST -> new UV(from.z(), 1F - to.y(), to.z(), 1F - from.y());
			case EAST -> new UV(1F - to.z(), 1F - to.y(), 1F - from.z(), 1F - from.y());
		};
	}

	public UV mul(UV uv) {
		return new UV(
			KMath.lerp(uv.u0, u0, u1),
			KMath.lerp(uv.v0, v0, v1),
			KMath.lerp(uv.u1, u0, u1),
			KMath.lerp(uv.v1, v0, v1)
		);
	}

	@Override
	public String toString() {
		return "[" + u0 + "," + v0 + "," + u1 + "," + v1 + "]";
	}
}
