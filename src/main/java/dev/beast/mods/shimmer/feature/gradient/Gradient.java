package dev.beast.mods.shimmer.feature.gradient;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.math.Color;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;

import java.util.function.Function;

public interface Gradient {
	Codec<Gradient> CODEC = Codec.either(ClientGradients.REGISTRY.valueCodec, Color.CODEC).xmap(either -> either.map(Function.identity(), Function.identity()), gradient -> gradient instanceof Color color ? Either.right(color) : Either.left(gradient));
	StreamCodec<ByteBuf, Gradient> STREAM_CODEC = ByteBufCodecs.either(ClientGradients.REGISTRY.valueStreamCodec, Color.STREAM_CODEC).map(either -> either.map(Function.identity(), Function.identity()), gradient -> gradient instanceof Color color ? Either.right(color) : Either.left(gradient));

	Color get(float delta);

	default Color sample(RandomSource random) {
		return get(random.nextFloat());
	}
}
