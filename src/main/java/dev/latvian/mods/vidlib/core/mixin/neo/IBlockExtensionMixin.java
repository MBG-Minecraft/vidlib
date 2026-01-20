package dev.latvian.mods.vidlib.core.mixin.neo;

import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IBlockExtension.class)
public interface IBlockExtensionMixin {
	@Inject(method = "isLadder", at = @At("HEAD"), cancellable = true)
	default void vl$isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
		if (CommonGameEngine.INSTANCE.isLadder(level, pos, state, entity)) {
			cir.setReturnValue(true);
		}
	}
}
