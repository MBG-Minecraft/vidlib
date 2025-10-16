package dev.latvian.mods.vidlib.math.knumber;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface KNumber {
	SimpleRegistry<KNumber> REGISTRY = SimpleRegistry.create(KNumber::type);

	Codec<KNumber> LITERAL_CODEC = Codec.either(Codec.DOUBLE, Codec.STRING).xmap(
		e -> e.map(KNumber::of, KNumber::named),
		v -> v instanceof FixedKNumber(Double number) ? Either.left(number) : Either.right(v.toString())
	);

	Codec<KNumber> CODEC = Codec.either(LITERAL_CODEC, REGISTRY.valueCodec()).xmap(
		e -> e.map(Function.identity(), Function.identity()),
		v -> v.isLiteral() ? Either.left(v) : Either.right(v)
	);

	StreamCodec<ByteBuf, KNumber> LITERAL_STREAM_CODEC = ByteBufCodecs.either(ByteBufCodecs.DOUBLE, ByteBufCodecs.STRING_UTF8).map(
		e -> e.map(KNumber::of, KNumber::named),
		v -> v instanceof FixedKNumber(Double number) ? Either.left(number) : Either.right(v.toString())
	);

	StreamCodec<RegistryFriendlyByteBuf, KNumber> STREAM_CODEC = ByteBufCodecs.either(LITERAL_STREAM_CODEC, REGISTRY.valueStreamCodec()).map(
		e -> e.map(Function.identity(), Function.identity()),
		v -> v.isLiteral() ? Either.left(v) : Either.right(v)
	);

	DataType<KNumber> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, KNumber.class);

	FixedKNumber ZERO = new FixedKNumber(0D);
	FixedKNumber ONE = new FixedKNumber(1D);
	SimpleRegistryType.Unit<FixedKNumber> ZERO_TYPE = SimpleRegistryType.unit("zero", ZERO);
	SimpleRegistryType.Unit<FixedKNumber> ONE_TYPE = SimpleRegistryType.unit("one", ONE);

	StreamCodec<RegistryFriendlyByteBuf, KNumber> OPTIONAL_ZERO_STREAM_CODEC = KLibStreamCodecs.optional(STREAM_CODEC, ZERO);
	StreamCodec<RegistryFriendlyByteBuf, KNumber> OPTIONAL_ONE_STREAM_CODEC = KLibStreamCodecs.optional(STREAM_CODEC, ONE);

	static FixedKNumber of(double number) {
		if (number == 0D) {
			return ZERO;
		} else if (number == 1D) {
			return ONE;
		} else {
			return new FixedKNumber(number);
		}
	}

	static KNumber named(String name) {
		if (name.startsWith("$")) {
			return new ServerDataKNumber(name.substring(1));
		} else {
			return new VariableKNumber(name);
		}
	}

	@AutoInit
	static void bootstrap() {
		REGISTRY.register(ZERO_TYPE);
		REGISTRY.register(ONE_TYPE);
		REGISTRY.register(FixedKNumber.TYPE);

		for (var literal : LiteralKNumber.values()) {
			REGISTRY.register(literal.type);
		}

		REGISTRY.register(InterpolatedKNumber.TYPE);
		REGISTRY.register(OffsetKNumber.TYPE);
		REGISTRY.register(ScaledKNumber.TYPE);
		REGISTRY.register(VariableKNumber.TYPE);
		REGISTRY.register(IfKNumber.TYPE);
		REGISTRY.register(ServerDataKNumber.TYPE);
		REGISTRY.register(RandomKNumber.TYPE);
		REGISTRY.register(SinKNumber.TYPE);
		REGISTRY.register(CosKNumber.TYPE);
		REGISTRY.register(Atan2KNumber.TYPE);
		REGISTRY.register(ClampedKNumber.TYPE);
	}

	default SimpleRegistryType<?> type() {
		return REGISTRY.getType(this);
	}

	@Nullable
	Double get(KNumberContext ctx);

	default boolean isLiteral() {
		return false;
	}

	default double getOr(KNumberContext ctx, double def) {
		Double value = get(ctx);
		return value == null ? def : value;
	}

	default double getOrNaN(KNumberContext ctx) {
		return getOr(ctx, Double.NaN);
	}

	default KNumber offset(KNumber other) {
		return new OffsetKNumber(this, other);
	}

	default KNumber scale(KNumber other) {
		return new ScaledKNumber(this, other);
	}
}
