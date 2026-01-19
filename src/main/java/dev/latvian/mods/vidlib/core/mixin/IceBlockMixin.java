package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IceBlock.class)
public class IceBlockMixin {
	@Inject(method = "melt", at = @At("HEAD"), cancellable = true)
	private void vl$melt(BlockState state, Level level, BlockPos pos, CallbackInfo ci) {
		if (CommonGameEngine.INSTANCE.disableIceMelting(level, pos, state)) {
			ci.cancel();
		}
	}
}
