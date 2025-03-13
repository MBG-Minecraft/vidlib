package dev.beast.mods.shimmer.core.mixin;

import net.minecraft.client.renderer.LevelEventHandler;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelEventHandler.class)
public class LevelEventHandlerMixin {
	/**
	 * Cancel sound that plays when you switch dimensions
	 */
	@Inject(method = "levelEvent", at = @At("HEAD"), cancellable = true)
	private void shimmer$levelEvent(int eventId, BlockPos pos, int data, CallbackInfo ci) {
		if (eventId == 1032) {
			ci.cancel();
		}
	}
}
