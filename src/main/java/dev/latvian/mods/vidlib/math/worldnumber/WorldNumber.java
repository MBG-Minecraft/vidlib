package dev.latvian.mods.vidlib.math.worldnumber;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kmath.easing.Easing;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Function;

public interface WorldNumber {
	SimpleRegistry<WorldNumber> REGISTRY = SimpleRegistry.create(WorldNumber::type);

	Codec<WorldNumber> LITERAL_CODEC = Codec.either(Codec.DOUBLE, Codec.STRING).xmap(
		e -> e.map(FixedWorldNumber::of, VariableWorldNumber::new),
		v -> v instanceof FixedWorldNumber(double number) ? Either.left(number) : Either.right(v.toString())
	);

	Codec<WorldNumber> CODEC = Codec.either(LITERAL_CODEC, REGISTRY.valueCodec()).xmap(
		e -> e.map(Function.identity(), Function.identity()),
		v -> v.isLiteral() ? Either.left(v) : Either.right(v)
	);

	StreamCodec<ByteBuf, WorldNumber> LITERAL_STREAM_CODEC = ByteBufCodecs.either(ByteBufCodecs.DOUBLE, ByteBufCodecs.STRING_UTF8).map(
		e -> e.map(FixedWorldNumber::of, VariableWorldNumber::new),
		v -> v instanceof FixedWorldNumber(double number) ? Either.left(number) : Either.right(v.toString())
	);

	StreamCodec<RegistryFriendlyByteBuf, WorldNumber> STREAM_CODEC = ByteBufCodecs.either(LITERAL_STREAM_CODEC, REGISTRY.valueStreamCodec()).map(
		e -> e.map(Function.identity(), Function.identity()),
		v -> v.isLiteral() ? Either.left(v) : Either.right(v)
	);

	@AutoInit
	static void bootstrap() {
		REGISTRY.register(FixedWorldNumber.ZERO);
		REGISTRY.register(FixedWorldNumber.ONE);
		REGISTRY.register(FixedWorldNumber.TYPE);
		REGISTRY.register(OffsetWorldNumber.TYPE);
		REGISTRY.register(ScaledWorldNumber.TYPE);
		REGISTRY.register(VariableWorldNumber.TYPE);
		REGISTRY.register(IfWorldNumber.TYPE);
		REGISTRY.register(InterpolatedWorldNumber.TYPE);
	}

	static WorldNumber fixed(double value) {
		return FixedWorldNumber.of(value);
	}

	default SimpleRegistryType<?> type() {
		return REGISTRY.getType(this);
	}

	double get(WorldNumberContext ctx);

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

	default boolean isLiteral() {
		return false;
	}
}
