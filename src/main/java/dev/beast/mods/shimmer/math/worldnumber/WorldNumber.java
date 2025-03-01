package dev.beast.mods.shimmer.math.worldnumber;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.math.Easing;
import dev.beast.mods.shimmer.util.registry.SimpleRegistry;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;

import java.util.function.Function;

public interface WorldNumber {
	SimpleRegistry<WorldNumber> REGISTRY = SimpleRegistry.create(WorldNumber::type);
	Codec<WorldNumber> CODEC = Codec.either(Codec.DOUBLE, REGISTRY.valueCodec()).xmap(either -> either.map(FixedWorldNumber::of, Function.identity()), num -> num instanceof FixedWorldNumber(double number) ? Either.left(number) : Either.right(num));
	StreamCodec<RegistryFriendlyByteBuf, WorldNumber> STREAM_CODEC = ByteBufCodecs.either(ByteBufCodecs.DOUBLE, REGISTRY.valueStreamCodec()).map(either -> either.map(FixedWorldNumber::of, Function.identity()), num -> num instanceof FixedWorldNumber(double number) ? Either.left(number) : Either.right(num));

	static void bootstrap() {
		REGISTRY.register(FixedWorldNumber.ZERO);
		REGISTRY.register(FixedWorldNumber.ONE);
		REGISTRY.register(FixedWorldNumber.TYPE);
		REGISTRY.register(OffsetWorldNumber.TYPE);
		REGISTRY.register(ScaledWorldNumber.TYPE);
		REGISTRY.register(InterpolatedWorldNumber.TYPE);
	}

	static WorldNumber fixed(double value) {
		return FixedWorldNumber.of(value);
	}

	default SimpleRegistryType<?> type() {
		return REGISTRY.getType(this);
	}

	double get(Level level, float progress);

	default WorldNumber offset(WorldNumber other) {
		return new OffsetWorldNumber(this, other);
	}

	default WorldNumber scale(WorldNumber other) {
		return new ScaledWorldNumber(this, other);
	}

	default WorldNumber interpolate(Easing easing, float start, float end, WorldNumber other) {
		return new InterpolatedWorldNumber(easing, start, end, this, other);
	}

	default WorldNumber interpolate(Easing easing, WorldNumber other) {
		return interpolate(easing, 0F, 1F, other);
	}
}
