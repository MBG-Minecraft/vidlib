package dev.latvian.mods.vidlib.feature.skybox;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.math.Range;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Function;

public record FogOverride(Range range, int shape, Color color) {
	public static final FogOverride DISABLED = new FogOverride(Range.ZERO, 0, Color.TRANSPARENT);
	public static final FogOverride DEFAULT = new FogOverride(Range.ZERO, 1, Color.TRANSPARENT);

	public static final Codec<FogOverride> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Range.CODEC.fieldOf("range").forGetter(FogOverride::range),
		Codec.INT.optionalFieldOf("shape", 0).forGetter(FogOverride::shape),
		Color.CODEC.optionalFieldOf("color", new Color(0xFFC0D8FF)).forGetter(FogOverride::color)
	).apply(instance, FogOverride::new));

	public static final Codec<FogOverride> CODEC = Codec.either(Codec.BOOL, DIRECT_CODEC).xmap(either -> either.map(b -> b ? DEFAULT : DISABLED, Function.identity()), f -> f.equals(DEFAULT) ? Either.left(true) : f.equals(DISABLED) ? Either.left(false) : Either.right(f));

	public static final StreamCodec<ByteBuf, FogOverride> STREAM_CODEC = CompositeStreamCodec.of(
		Range.STREAM_CODEC, FogOverride::range,
		ByteBufCodecs.VAR_INT, FogOverride::shape,
		Color.STREAM_CODEC, FogOverride::color,
		FogOverride::new
	);

	public static final DataType<FogOverride> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, FogOverride.class);
}
