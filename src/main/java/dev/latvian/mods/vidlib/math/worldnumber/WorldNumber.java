package dev.latvian.mods.vidlib.math.worldnumber;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
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

public interface WorldNumber {
	SimpleRegistry<WorldNumber> REGISTRY = SimpleRegistry.create(WorldNumber::type);
	ImBuilderHolderList<WorldNumber> IMGUI_BUILDERS = new ImBuilderHolderList<>();

	static WorldNumber named(String name) {
		if (name.startsWith("$")) {
			return new ServerDataWorldNumber(name.substring(1));
		} else {
			return new VariableWorldNumber(name);
		}
	}

	Codec<WorldNumber> LITERAL_CODEC = Codec.either(Codec.DOUBLE, Codec.STRING).xmap(
		e -> e.map(FixedWorldNumber::of, WorldNumber::named),
		v -> v instanceof FixedWorldNumber(Double number) ? Either.left(number) : Either.right(v.toString())
	);

	Codec<WorldNumber> CODEC = Codec.either(LITERAL_CODEC, REGISTRY.valueCodec()).xmap(
		e -> e.map(Function.identity(), Function.identity()),
		v -> v.isLiteral() ? Either.left(v) : Either.right(v)
	);

	StreamCodec<ByteBuf, WorldNumber> LITERAL_STREAM_CODEC = ByteBufCodecs.either(ByteBufCodecs.DOUBLE, ByteBufCodecs.STRING_UTF8).map(
		e -> e.map(FixedWorldNumber::of, WorldNumber::named),
		v -> v instanceof FixedWorldNumber(Double number) ? Either.left(number) : Either.right(v.toString())
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
		REGISTRY.register(InterpolatedWorldNumber.TYPE);
		REGISTRY.register(OffsetWorldNumber.TYPE);
		REGISTRY.register(ScaledWorldNumber.TYPE);
		REGISTRY.register(VariableWorldNumber.TYPE);
		REGISTRY.register(IfWorldNumber.TYPE);
		REGISTRY.register(ServerDataWorldNumber.TYPE);
		REGISTRY.register(RandomWorldNumber.TYPE);

		IMGUI_BUILDERS.add(FixedWorldNumber.Builder.TYPE);
		IMGUI_BUILDERS.add(InterpolatedWorldNumber.Builder.TYPE);
		IMGUI_BUILDERS.add(OffsetWorldNumber.Builder.TYPE);
		IMGUI_BUILDERS.add(ScaledWorldNumber.Builder.TYPE);
		IMGUI_BUILDERS.add(VariableWorldNumber.Builder.TYPE);
		IMGUI_BUILDERS.add(IfWorldNumber.Builder.TYPE);
		IMGUI_BUILDERS.add(ServerDataWorldNumber.Builder.TYPE);
		IMGUI_BUILDERS.add(RandomWorldNumber.Builder.TYPE);
	}

	static WorldNumber fixed(double value) {
		return FixedWorldNumber.of(value);
	}

	default SimpleRegistryType<?> type() {
		return REGISTRY.getType(this);
	}

	@Nullable
	Double get(WorldNumberContext ctx);

	default boolean isLiteral() {
		return false;
	}

	default double getOr(WorldNumberContext ctx, double def) {
		Double value = get(ctx);
		return value == null ? def : value;
	}

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
