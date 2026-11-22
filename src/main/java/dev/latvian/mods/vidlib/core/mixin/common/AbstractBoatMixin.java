package dev.latvian.mods.vidlib.core.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.latvian.mods.vidlib.core.api.VLBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractBoat.class)
public class AbstractBoatMixin {
	@WrapOperation(method = {"getWaterLevelAbove", "checkInWater", "isUnderwater", "checkFallDamage"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"))
	private FluidState vl$getFluidState(Level level, BlockPos blockPos, Operation<FluidState> original) {
		return VLBridge.overrideFluidState.apply(level, blockPos);
	}

	@WrapOperation(method = "getGroundFriction", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
	private BlockState vl$getBlockState(Level level, BlockPos blockPos, Operation<BlockState> original) {
		var state = VLBridge.overrideFluidStateBlock.apply(level, blockPos);
		return state.isAir() ? original.call(level, blockPos) : state;
	}
}
