package dev.latvian.mods.vidlib.core.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TallGrassBlock.class)
public class TallGrassBlockMixin {
	@Unique
	private static final VoxelShape BC$SHAPE = Block.column(12D, 0D, 7D);

	@Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
	private void bc$getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
		if (blockGetter instanceof Level level && level.getEnvironment().isServerNeoForge()) {
			cir.setReturnValue(BC$SHAPE.move(state.getOffset(pos)));
		}
	}
}
