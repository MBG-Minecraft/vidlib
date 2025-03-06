package dev.beast.mods.shimmer.math.worldnumber;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.math.worldposition.WorldPosition;
import dev.beast.mods.shimmer.util.ShimmerStreamCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.Map;

public record WorldNumberVariables(Map<String, WorldNumber> numbers, Map<String, WorldPosition> positions) {
	public static final WorldNumberVariables EMPTY = new WorldNumberVariables(Map.of(), Map.of());

	public static final Codec<WorldNumberVariables> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.unboundedMap(Codec.STRING, WorldNumber.CODEC).optionalFieldOf("numbers", Map.of()).forGetter(WorldNumberVariables::numbers),
		Codec.unboundedMap(Codec.STRING, WorldPosition.CODEC).optionalFieldOf("positions", Map.of()).forGetter(WorldNumberVariables::positions)
	).apply(instance, WorldNumberVariables::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, WorldNumberVariables> STREAM_CODEC = StreamCodec.composite(
		ShimmerStreamCodecs.unboundedMap(ByteBufCodecs.STRING_UTF8, WorldNumber.STREAM_CODEC),
		WorldNumberVariables::numbers,
		ShimmerStreamCodecs.unboundedMap(ByteBufCodecs.STRING_UTF8, WorldPosition.STREAM_CODEC),
		WorldNumberVariables::positions,
		WorldNumberVariables::new
	);

	public static WorldNumberVariables pos(String name, WorldPosition pos) {
		return new WorldNumberVariables(Map.of(), Map.of(name, pos));
	}

	public static WorldNumberVariables num(String name, WorldNumber num) {
		return new WorldNumberVariables(Map.of(name, num), Map.of());
	}

	public WorldNumberVariables merge(WorldNumberVariables other) {
		var numbers = new HashMap<>(numbers());
		numbers.putAll(other.numbers());
		var positions = new HashMap<>(positions());
		positions.putAll(other.positions());
		return new WorldNumberVariables(numbers, positions);
	}
}
