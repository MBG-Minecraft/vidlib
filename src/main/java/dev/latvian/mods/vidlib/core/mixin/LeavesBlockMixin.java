package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LeavesBlock.class)
public class LeavesBlockMixin {
	@Inject(method = "decaying", at = @At("HEAD"), cancellable = true)
	protected void decaying(BlockState state, CallbackInfoReturnable<Boolean> cir) {
		if (CommonGameEngine.INSTANCE.disableLeafDecay(state)) {
			cir.setReturnValue(false);
		}
	}
}
