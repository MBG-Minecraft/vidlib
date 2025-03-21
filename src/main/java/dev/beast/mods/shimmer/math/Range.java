package dev.beast.mods.shimmer.math;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;

import java.util.List;

@AutoInit
public record Range(float min, float max) {
	public static final Range ZERO = new Range(0F, 0F);
	public static final Range ONE = new Range(1F, 1F);
	public static final Range FULL = new Range(0F, 1F);

	public static final Codec<Range> CODEC = Codec.either(Codec.FLOAT, Codec.FLOAT.listOf(2, 2)).xmap(either -> either.map(Range::of, list -> of(list.getFirst(), list.getLast())), range -> range.min == range.max ? Either.left(range.min) : Either.right(List.of(range.min, range.max)));

	public static final StreamCodec<ByteBuf, Range> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.FLOAT, Range::min,
		ByteBufCodecs.FLOAT, Range::max,
		Range::new
	);

	public static final KnownCodec<Range> KNOWN_CODEC = KnownCodec.register(Shimmer.id("range"), CODEC, STREAM_CODEC, Range.class);

	public static Range of(float min, float max) {
		return min == 0F && max == 0F ? ZERO : min == 1F && max == 1F ? ONE : Math.abs(max - min) < 0.0001F ? new Range(min, min) : new Range(Math.min(min, max), Math.max(min, max));
	}

	public static Range of(float value) {
		return of(value, value);
	}

	public static Range of(Tag nbt) {
		var fp = FloatPair.of(nbt);
		return fp == null ? null : of(fp.a(), fp.b());
	}

	public Tag toNBT() {
		return FloatPair.toNBT(min, max);
	}

	public float get(float delta) {
		return min == max ? min : delta * (max - min) + min;
	}

	public float get(RandomSource random) {
		return min == max ? min : random.nextFloat() * (max - min) + min;
	}

	public float delta(float value) {
		return (value - min) / (max - min);
	}

	@Override
	public String toString() {
		return min == max ? KMath.format(min) : (KMath.format(min) + " - " + KMath.format(max));
	}

	public float clamp(float value) {
		return Math.clamp(value, min, max);
	}
}
