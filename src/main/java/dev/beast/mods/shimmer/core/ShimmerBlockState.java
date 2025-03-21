package dev.beast.mods.shimmer.core;

import net.minecraft.core.registries.BuiltInRegistries;

public interface ShimmerBlockState {
	static void shimmer$clearAllCache() {
		for (var block : BuiltInRegistries.BLOCK) {
			for (var state : block.getStateDefinition().getPossibleStates()) {
				state.shimmer$clearCache();
			}
		}
	}

	default Object shimmer$clientProperties() {
		throw new NoMixinException(this);
	}

	default void shimmer$clearCache() {
	}

	default float shimmer$getDensity() {
		throw new NoMixinException(this);
	}
}
