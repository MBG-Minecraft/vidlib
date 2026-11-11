package dev.latvian.mods.vidlib.core.mixin.common;

import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
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
		return CommonGameEngine.INSTANCE.overrideFluidState(level, blockPos);
	}

	@Redirect(method = "getGroundFriction", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
	private BlockState vl$getBlockState(Level level, BlockPos blockPos) {
		var state = CommonGameEngine.INSTANCE.overrideFluidStateBlock(level, blockPos);
		return state.isAir() ? level.getBlockState(blockPos) : state;
	}
}
