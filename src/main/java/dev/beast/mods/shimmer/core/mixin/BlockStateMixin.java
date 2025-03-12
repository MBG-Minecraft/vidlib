package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerBlockState;
import dev.beast.mods.shimmer.feature.block.ShimmerBlockStateClientProperties;
import dev.beast.mods.shimmer.util.WithCache;
import net.minecraft.world.level.block.BarrierBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockState.class)
public class BlockStateMixin implements ShimmerBlockState {
	@Unique
	private Object shimmer$clientProperties;

	@Unique
	private float shimmer$density = Float.NaN;

	@Override
	public void shimmer$clearCache() {
		if (shimmer$clientProperties instanceof WithCache cache) {
			cache.clearCache();
			shimmer$clientProperties = null;
		}
	}

	@Override
	public Object shimmer$clientProperties() {
		if (shimmer$clientProperties == null) {
			shimmer$clientProperties = new ShimmerBlockStateClientProperties((BlockState) (Object) this);
		}

		return shimmer$clientProperties;
	}

	@Override
	public float shimmer$getDensity() {
		if (Float.isNaN(shimmer$density)) {
			var state = (BlockState) (Object) this;
			var b = state.getBlock();

			if (state.isAir() || b instanceof LightBlock || b instanceof BarrierBlock || b instanceof FireBlock) {
				shimmer$density = 0F;
			} else if (b instanceof CarpetBlock || b instanceof ButtonBlock || b instanceof PressurePlateBlock) {
				shimmer$density = 0.06125F;
			} else if (b instanceof DoorBlock || b instanceof SnowLayerBlock) {
				shimmer$density = 0.125F;
			} else if (b instanceof BushBlock) {
				shimmer$density = 0.25F;
			} else if (b instanceof SlabBlock || b instanceof CrossCollisionBlock || b instanceof FenceGateBlock) {
				shimmer$density = 0.5F;
			} else if (b instanceof StairBlock || b instanceof WallBlock) {
				shimmer$density = 0.75F;
			} else {
				shimmer$density = 1F;
			}
		}

		return shimmer$density;
	}
}
