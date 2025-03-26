package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.util.Cast;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

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

	default String shimmer$toString() {
		var state = (BlockState) this;
		var sb = new StringBuilder();
		sb.append(state.getBlock().builtInRegistryHolder().getKey().location());
		boolean first = true;

		for (var prop : state.getProperties()) {
			var value = state.getValue(prop);

			if (!value.equals(state.getBlock().defaultBlockState().getValue(prop))) {
				if (first) {
					sb.append('[');
					first = false;
				} else {
					sb.append(',');
				}

				sb.append(prop.getName());
				sb.append('=');
				sb.append(prop.getName(Cast.to(value)));
			}
		}

		if (!first) {
			sb.append(']');
		}

		return sb.toString();
	}
}
