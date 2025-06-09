package dev.latvian.mods.vidlib.math.worldnumber;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.math.worldvector.WorldVector;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.Map;

public record WorldNumberVariables(Map<String, WorldNumber> numbers, Map<String, WorldVector> vectors) {
	public static final WorldNumberVariables EMPTY = new WorldNumberVariables(Map.of(), Map.of());

	public static final Codec<WorldNumberVariables> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.unboundedMap(Codec.STRING, WorldNumber.CODEC).optionalFieldOf("numbers", Map.of()).forGetter(WorldNumberVariables::numbers),
		Codec.unboundedMap(Codec.STRING, WorldVector.CODEC).optionalFieldOf("vectors", Map.of()).forGetter(WorldNumberVariables::vectors)
	).apply(instance, WorldNumberVariables::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, WorldNumberVariables> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.STRING_UTF8.unboundedMap(WorldNumber.STREAM_CODEC), WorldNumberVariables::numbers,
		ByteBufCodecs.STRING_UTF8.unboundedMap(WorldVector.STREAM_CODEC), WorldNumberVariables::vectors,
		WorldNumberVariables::new
	);

	public static WorldNumberVariables vec(String name, WorldVector vec) {
		return new WorldNumberVariables(Map.of(), Map.of(name, vec));
	}

	public static WorldNumberVariables num(String name, WorldNumber num) {
		return new WorldNumberVariables(Map.of(name, num), Map.of());
	}

	public WorldNumberVariables() {
		this(new HashMap<>(), new HashMap<>());
	}

	public WorldNumberVariables merge(WorldNumberVariables other) {
		if (numbers.isEmpty() && vectors.isEmpty()) {
			return other;
		} else if (other.numbers.isEmpty() && other.vectors.isEmpty()) {
			return this;
		}

		var numbers = new HashMap<>(numbers());
		numbers.putAll(other.numbers());
		var positions = new HashMap<>(vectors());
		positions.putAll(other.vectors());
		return new WorldNumberVariables(numbers, positions);
	}

	public void replace(WorldNumberVariables variables) {
		numbers.clear();
		numbers.putAll(variables.numbers);
		vectors.clear();
		vectors.putAll(variables.vectors);
	}
}
