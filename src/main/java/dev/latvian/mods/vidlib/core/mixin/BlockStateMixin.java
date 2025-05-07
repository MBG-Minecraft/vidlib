package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLBlockState;
import dev.latvian.mods.vidlib.feature.block.VidLibBlockStateClientProperties;
import dev.latvian.mods.vidlib.util.WithCache;
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
public class BlockStateMixin implements VLBlockState {
	@Unique
	private Object vl$clientProperties;

	@Unique
	private float vl$density = Float.NaN;

	@Unique
	private Boolean vl$visible;

	@Override
	public void vl$clearCache() {
		if (vl$clientProperties instanceof WithCache cache) {
			cache.clearCache();
			vl$clientProperties = null;
		}
	}

	@Override
	public Object vl$clientProperties() {
		if (vl$clientProperties == null) {
			vl$clientProperties = new VidLibBlockStateClientProperties((BlockState) (Object) this);
		}

		return vl$clientProperties;
	}

	@Override
	public float vl$getDensity() {
		if (Float.isNaN(vl$density)) {
			var state = (BlockState) (Object) this;
			var b = state.getBlock();

			if (state.isAir() || b instanceof LightBlock || b instanceof BarrierBlock || b instanceof FireBlock) {
				vl$density = 0F;
			} else if (b instanceof CarpetBlock || b instanceof ButtonBlock || b instanceof PressurePlateBlock) {
				vl$density = 0.06125F;
			} else if (b instanceof DoorBlock || b instanceof SnowLayerBlock) {
				vl$density = 0.125F;
			} else if (b instanceof BushBlock) {
				vl$density = 0.25F;
			} else if (b instanceof SlabBlock || b instanceof CrossCollisionBlock || b instanceof FenceGateBlock) {
				vl$density = 0.5F;
			} else if (b instanceof StairBlock || b instanceof WallBlock) {
				vl$density = 0.75F;
			} else {
				vl$density = 1F;
			}
		}

		return vl$density;
	}

	@Override
	public boolean isVisible() {
		if (vl$visible == null) {
			vl$visible = VLBlockState.super.isVisible();
		}

		return vl$visible;
	}
}
