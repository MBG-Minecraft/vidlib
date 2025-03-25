package dev.beast.mods.shimmer.feature.gradient;

import dev.beast.mods.shimmer.math.Color;
import net.minecraft.util.RandomSource;

public interface Gradient {
	Color get(float delta);

	default Color sample(RandomSource random) {
		return get(random.nextFloat());
	}
}
