package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.util.Cast;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

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

	default float vl$getDensity() {
		throw new NoMixinException(this);
	}

	default String vl$toString() {
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

	default boolean isVisible() {
		var state = (BlockState) this;
		return state.getRenderShape() != RenderShape.INVISIBLE || !state.getFluidState().isEmpty();
	}
}
