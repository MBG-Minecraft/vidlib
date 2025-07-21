package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.util.Cast;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.BarrierBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.VineBlock;
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
		var state = (BlockState) this;
		var b = state.getBlock();

		if (state.isAir() || b instanceof LightBlock || b instanceof BarrierBlock || b instanceof FireBlock) {
			return 0F;
		} else if (b instanceof CarpetBlock || b instanceof ButtonBlock || b instanceof PressurePlateBlock || b instanceof VineBlock || b instanceof LadderBlock) {
			return 0.06125F;
		} else if (b instanceof DoorBlock || b instanceof SnowLayerBlock || b instanceof FlowerPotBlock) {
			return 0.125F;
		} else if (b instanceof SlabBlock || b instanceof CrossCollisionBlock || b instanceof FenceGateBlock || b instanceof EnchantingTableBlock) {
			return 0.5F;
		} else if (b instanceof VegetationBlock) {
			return 0.25F;
		} else if (b instanceof SimpleWaterloggedBlock || b instanceof HopperBlock) {
			return 0.75F;
		} else {
			return 1F;
		}
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

	default boolean isPartial() {
		var state = (BlockState) this;

		var b = state.getBlock();

		if (b instanceof HalfTransparentBlock || b instanceof SimpleWaterloggedBlock || !state.getFluidState().isEmpty()) {
			return true;
		}

		return !isVisible() || vl$getDensity() < 1F;
	}
}
