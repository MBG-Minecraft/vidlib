package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.VidLibConfig;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FallingBlock.class)
public class FallingBlockMixin {
	@Inject(method = "isFree", at = @At("HEAD"), cancellable = true)
	private static void vl$isFree(BlockState state, CallbackInfoReturnable<Boolean> cir) {
		if (VidLibConfig.disableFallingBlocks) {
			cir.setReturnValue(false);
		}
	}
}
