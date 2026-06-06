package dev.latvian.mods.vidlib.feature.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.color.Gradient;

import java.util.function.Predicate;

public interface VLCodecs {
	static <V, T> Codec<T> unit(V unitValue, T resultValue, Predicate<T> isUnit) {
		return Codec.unit(unitValue).flatXmap(value -> {
			if (unitValue.equals(value)) {
				return DataResult.success(resultValue);
			} else {
				return DataResult.error(() -> "Not unit");
			}
		}, value -> {
			if (isUnit.test(value)) {
				return DataResult.success(unitValue);
			} else {
				return DataResult.error(() -> "Not empty value");
			}
		});
	}

	Codec<Gradient> TRANSPARENT_GRADIENT_UNIT_CODEC = unit("", Color.TRANSPARENT, Color.TRANSPARENT::equals);
	Codec<Gradient> TRANSPARENT_OR_GRADIENT_CODEC = KLibCodecs.or(TRANSPARENT_GRADIENT_UNIT_CODEC, Gradient.CODEC);
}
