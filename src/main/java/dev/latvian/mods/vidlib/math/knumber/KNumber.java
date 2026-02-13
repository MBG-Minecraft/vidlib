package dev.latvian.mods.vidlib.math.knumber;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryCollector;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryEntry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface KNumber extends SimpleRegistryEntry {
	SimpleRegistry<KNumber> REGISTRY = SimpleRegistry.create(VidLib.id("knumber"), c -> PlatformHelper.CURRENT.collectKNumbers(c));

	FixedKNumber ZERO = new FixedKNumber(0D);
	FixedKNumber ONE = new FixedKNumber(1D);
	SimpleRegistryType.Unit<FixedKNumber> ZERO_TYPE = SimpleRegistryType.unit("zero", ZERO);
	SimpleRegistryType.Unit<FixedKNumber> ONE_TYPE = SimpleRegistryType.unit("one", ONE);

	Codec<KNumber> LITERAL_CODEC = Codec.either(Codec.DOUBLE, Codec.STRING).xmap(
		e -> e.map(KNumber::of, KNumber::named),
		v -> v instanceof FixedKNumber(Double number) ? Either.left(number) : Either.right(v.toString())
	);

	Codec<KNumber> CODEC = Codec.either(LITERAL_CODEC, REGISTRY.codec()).xmap(
		e -> e.map(Function.identity(), Function.identity()),
		v -> v.isLiteral() ? Either.left(v) : Either.right(v)
	);

	StreamCodec<ByteBuf, KNumber> LITERAL_STREAM_CODEC = ByteBufCodecs.either(ByteBufCodecs.DOUBLE, ByteBufCodecs.STRING_UTF8).map(
		e -> e.map(KNumber::of, KNumber::named),
		v -> v instanceof FixedKNumber(Double number) ? Either.left(number) : Either.right(v.toString())
	);

	StreamCodec<RegistryFriendlyByteBuf, KNumber> STREAM_CODEC = ByteBufCodecs.either(LITERAL_STREAM_CODEC, REGISTRY.streamCodec()).map(
		e -> e.map(Function.identity(), Function.identity()),
		v -> v.isLiteral() ? Either.left(v) : Either.right(v)
	);

	DataType<KNumber> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, KNumber.class);

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

	static void builtinTypes(SimpleRegistryCollector<KNumber> registry) {
		registry.register(ZERO_TYPE);
		registry.register(ONE_TYPE);
		registry.register(FixedKNumber.TYPE);

		for (var literal : LiteralKNumber.values()) {
			registry.register(literal.type);
		}

		registry.register(InterpolatedKNumber.TYPE);
		registry.register(OffsetKNumber.TYPE);
		registry.register(ScaledKNumber.TYPE);
		registry.register(VariableKNumber.TYPE);
		registry.register(IfKNumber.TYPE);
		registry.register(ServerDataKNumber.TYPE);
		registry.register(RandomKNumber.TYPE);
		registry.register(SinKNumber.TYPE);
		registry.register(CosKNumber.TYPE);
		registry.register(Atan2KNumber.TYPE);
		registry.register(ClampedKNumber.TYPE);
	}

	@Override
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
