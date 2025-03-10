package dev.beast.mods.shimmer.math;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.util.CompositeStreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record Size3f(float x, float y, float z) {
	public static final Size3f ZERO = new Size3f(0F, 0F, 0F);
	public static final Size3f ONE = new Size3f(1F, 1F, 1F);

	public static Size3f of(float size) {
		return size == 0F ? ZERO : size == 1F ? ONE : new Size3f(size, size, size);
	}

	public static Size3f of(float x, float y, float z) {
		return x == y && x == z ? of(x) : new Size3f(x, y, z);
	}

	public static final Codec<Size3f> CODEC = Codec.either(Codec.FLOAT, Codec.FLOAT.listOf()).xmap(
		either -> either.map(Size3f::of, list -> switch (list.size()) {
			case 1 -> of(list.get(0));
			case 3 -> of(list.get(0), list.get(1), list.get(2));
			default -> throw new IllegalArgumentException("Invalid Size3f list size: " + list.size());
		}),
		r -> {
			if (r.x == r.y && r.x == r.z) {
				return Either.left(r.x);
			} else {
				return Either.right(List.of(r.x, r.y, r.z));
			}
		}
	);

	public static final StreamCodec<ByteBuf, Size3f> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.FLOAT,
		Size3f::x,
		ByteBufCodecs.FLOAT,
		Size3f::y,
		ByteBufCodecs.FLOAT,
		Size3f::z,
		Size3f::of
	);

	public Size3f scale(float scale) {
		return of(x * scale, y * scale, z * scale);
	}
}
