package dev.latvian.mods.vidlib.math.knumber;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.easing.Easing;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilderHolderList;
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
	ImBuilderHolderList<KNumber> IMGUI_BUILDERS = new ImBuilderHolderList<>();

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
		REGISTRY.register(InterpolatedKNumber.TYPE);
		REGISTRY.register(OffsetKNumber.TYPE);
		REGISTRY.register(ScaledKNumber.TYPE);
		REGISTRY.register(VariableKNumber.TYPE);
		REGISTRY.register(IfKNumber.TYPE);
		REGISTRY.register(ServerDataKNumber.TYPE);
		REGISTRY.register(RandomKNumber.TYPE);
		REGISTRY.register(TimeKNumber.TYPE);
		REGISTRY.register(GameTimeKNumber.TYPE);
		REGISTRY.register(DayTimeKNumber.TYPE);
		REGISTRY.register(SinKNumber.TYPE);
		REGISTRY.register(CosKNumber.TYPE);
		REGISTRY.register(Atan2KNumber.TYPE);

		IMGUI_BUILDERS.add(FixedKNumber.Builder.TYPE);
		IMGUI_BUILDERS.add(InterpolatedKNumber.Builder.TYPE);
		IMGUI_BUILDERS.add(OffsetKNumber.Builder.TYPE);
		IMGUI_BUILDERS.add(ScaledKNumber.Builder.TYPE);
		IMGUI_BUILDERS.add(VariableKNumber.Builder.TYPE);
		IMGUI_BUILDERS.add(IfKNumber.Builder.TYPE);
		IMGUI_BUILDERS.add(ServerDataKNumber.Builder.TYPE);
		IMGUI_BUILDERS.add(RandomKNumber.Builder.TYPE);
		IMGUI_BUILDERS.addUnit("Time", TimeKNumber.INSTANCE);
		IMGUI_BUILDERS.addUnit("Game Time", GameTimeKNumber.INSTANCE);
		IMGUI_BUILDERS.addUnit("Day Time", DayTimeKNumber.INSTANCE);
		IMGUI_BUILDERS.add(SinKNumber.Builder.TYPE);
		IMGUI_BUILDERS.add(CosKNumber.Builder.TYPE);
		IMGUI_BUILDERS.add(Atan2KNumber.Builder.TYPE);
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

	default KNumber offset(KNumber other) {
		return new OffsetKNumber(this, other);
	}

	default KNumber scale(KNumber other) {
		return new ScaledKNumber(this, other);
	}

	default KNumber interpolate(Easing easing, float start, float end, KNumber other) {
		return new InterpolatedKNumber(easing, start, end, this, other);
	}

	default KNumber interpolate(Easing easing, KNumber other) {
		return interpolate(easing, 0F, 1F, other);
	}
}
