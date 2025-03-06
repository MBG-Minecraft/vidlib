package dev.beast.mods.shimmer.math;

import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.util.CompositeStreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.Util;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.stream.IntStream;

public record Size2(int w, int h) {
	public static final Size2 ZERO = new Size2(0, 0);
	public static final Codec<Size2> CODEC = Codec.INT_STREAM.comapFlatMap(r -> Util.fixedSize(r, 2).map(i -> new Size2(i[0], i[1])), s -> IntStream.of(s.w(), s.h())).stable();

	public static final StreamCodec<ByteBuf, Size2> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT,
		Size2::w,
		ByteBufCodecs.VAR_INT,
		Size2::h,
		Size2::new
	);
}
