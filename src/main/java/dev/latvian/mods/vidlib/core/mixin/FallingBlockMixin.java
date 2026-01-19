package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FallingBlock.class)
public class FallingBlockMixin {
	@Inject(method = "onPlace", at = @At("HEAD"), cancellable = true)
	private void vl$place(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving, CallbackInfo ci) {
		if (CommonGameEngine.INSTANCE.disableBlockGravity(level, pos, state)) {
			ci.cancel();
		}
	}

	@Inject(method = "updateShape", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ScheduledTickAccess;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;I)V"), cancellable = true)
	private void vl$update(BlockState state, LevelReader levelReader, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos pos1, BlockState state1, RandomSource random, CallbackInfoReturnable<BlockState> cir) {
		if (levelReader instanceof Level level && CommonGameEngine.INSTANCE.disableBlockGravity(level, pos, state) || tickAccess instanceof Level level1 && CommonGameEngine.INSTANCE.disableBlockGravity(level1, pos, state)) {
			cir.setReturnValue(state);
		}
	}

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void vl$tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
		if (CommonGameEngine.INSTANCE.disableBlockGravity(level, pos, state)) {
			ci.cancel();
		}
	}
}
