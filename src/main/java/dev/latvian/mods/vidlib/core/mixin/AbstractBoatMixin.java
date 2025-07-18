package dev.latvian.mods.vidlib.core.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractBoat.class)
public class AbstractBoatMixin {
	@Redirect(method = {"getWaterLevelAbove", "checkInWater", "isUnderwater", "checkFallDamage"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"))
	private FluidState vl$getFluidState(Level level, BlockPos blockPos) {
		return level.vl$overrideFluidState(blockPos);
	}

	@Redirect(method = {"getWaterLevelAbove", "checkInWater", "isUnderwater"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;getHeight(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"))
	private float vl$getFluidHeight(FluidState state, BlockGetter blockGetter, BlockPos pos) {
		return blockGetter instanceof Level level ? level.vl$overrideFluidHeight(state, pos) : state.getHeight(blockGetter, pos);
	}

	@Redirect(method = "getGroundFriction", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
	private BlockState vl$getBlockState(Level level, BlockPos blockPos) {
		return level.vl$overrideFluidStateBlock(blockPos);
	}
}
