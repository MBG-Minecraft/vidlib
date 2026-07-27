package dev.latvian.mods.vidlib.core;

import net.minecraft.core.registries.BuiltInRegistries;

public interface VLBlockState {
	static void vl$clearAllCache() {
		for (var block : BuiltInRegistries.BLOCK) {
			for (var state : block.getStateDefinition().getPossibleStates()) {
				state.vl$clearCache();
			}
		}
	}

	default Object vl$clientProperties() {
		throw new NoMixinException(this);
	}

	default void vl$clearCache() {
	}
}
