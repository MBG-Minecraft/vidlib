package dev.latvian.mods.vidlib.core.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.latvian.mods.vidlib.core.api.VLBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CoralBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CoralBlock.class)
public class CoralBlockMixin {
	@WrapOperation(method = "updateShape", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ScheduledTickAccess;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;I)V"))
	private void vl$updateShape(ScheduledTickAccess instance, BlockPos pos, Block block, int tickDelay, Operation<Void> original) {
		if (VLBridge.tickCoralBlocks.apply(instance, pos, block)) {
			original.call(instance, pos, block, tickDelay);
		}
	}

	@WrapOperation(method = "getStateForPlacement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;I)V"))
	private void vl$getStateForPlacement(Level instance, BlockPos pos, Block block, int tickDelay, Operation<Void> original) {
		if (VLBridge.tickCoralBlocks.apply(instance, pos, block)) {
			original.call(instance, pos, block, tickDelay);
		}
	}
}
