package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.ShimmerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CoralBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CoralBlock.class)
public class CoralBlockMixin {
	@Redirect(method = "updateShape", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelAccessor;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;I)V"))
	private void shimmer$updateShape(LevelAccessor instance, BlockPos pos, Block block, int delay) {
		if (!ShimmerConfig.disableCoralBlocks) {
			instance.scheduleTick(pos, block, delay);
		}
	}

	@Redirect(method = "getStateForPlacement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;I)V"))
	private void shimmer$getStateForPlacement(Level instance, BlockPos pos, Block block, int delay) {
		if (!ShimmerConfig.disableCoralBlocks) {
			instance.scheduleTick(pos, block, delay);
		}
	}
}
