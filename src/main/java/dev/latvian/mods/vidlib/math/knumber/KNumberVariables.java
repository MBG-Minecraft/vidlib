package dev.latvian.mods.vidlib.math.knumber;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Map;

public record KNumberVariables(Map<String, KNumber> numbers, Map<String, KVector> vectors) {
	public static final KNumberVariables EMPTY = new KNumberVariables(Map.of(), Map.of());

	public static final Codec<KNumberVariables> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.unboundedMap(Codec.STRING, KNumber.CODEC).optionalFieldOf("numbers", Map.of()).forGetter(KNumberVariables::numbers),
		Codec.unboundedMap(Codec.STRING, KVector.CODEC).optionalFieldOf("vectors", Map.of()).forGetter(KNumberVariables::vectors)
	).apply(instance, KNumberVariables::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, KNumberVariables> STREAM_CODEC = CompositeStreamCodec.of(
		KLibStreamCodecs.unboundedMap(ByteBufCodecs.STRING_UTF8, KNumber.STREAM_CODEC), KNumberVariables::numbers,
		KLibStreamCodecs.unboundedMap(ByteBufCodecs.STRING_UTF8, KVector.STREAM_CODEC), KNumberVariables::vectors,
		KNumberVariables::new
	);

	public static KNumberVariables vec(String name, KVector vec) {
		return new KNumberVariables(Map.of(), Map.of(name, vec));
	}

	public static KNumberVariables num(String name, KNumber num) {
		return new KNumberVariables(Map.of(name, num), Map.of());
	}

	public KNumberVariables() {
		this(new Object2ObjectOpenHashMap<>(), new Object2ObjectOpenHashMap<>());
	}

	public KNumberVariables merge(KNumberVariables other) {
		if (isEmpty()) {
			return other;
		} else if (other.isEmpty()) {
			return this;
		}

		var numbers = new Object2ObjectOpenHashMap<>(numbers());
		numbers.putAll(other.numbers());
		var positions = new Object2ObjectOpenHashMap<>(vectors());
		positions.putAll(other.vectors());
		return new KNumberVariables(numbers, positions);
	}

	public void replace(KNumberVariables variables) {
		numbers.clear();
		numbers.putAll(variables.numbers);
		vectors.clear();
		vectors.putAll(variables.vectors);
	}

	public boolean isEmpty() {
		return numbers.isEmpty() && vectors.isEmpty();
	}
}
