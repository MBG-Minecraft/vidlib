package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CoralBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CoralBlock.class)
public class CoralBlockMixin {
	@Redirect(method = "updateShape", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ScheduledTickAccess;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;I)V"))
	private void vl$updateShape(ScheduledTickAccess instance, BlockPos pos, Block block, int delay) {
		if (CommonGameEngine.INSTANCE.tickCoralBlocks(instance, pos, block)) {
			instance.scheduleTick(pos, block, delay);
		}
	}

	@Redirect(method = "getStateForPlacement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;I)V"))
	private void vl$getStateForPlacement(Level instance, BlockPos pos, Block block, int delay) {
		if (CommonGameEngine.INSTANCE.tickCoralBlocks(instance, pos, block)) {
			instance.scheduleTick(pos, block, delay);
		}
	}
}
