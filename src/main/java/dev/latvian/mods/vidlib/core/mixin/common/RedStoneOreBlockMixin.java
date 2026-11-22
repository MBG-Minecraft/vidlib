package dev.latvian.mods.vidlib.core.mixin.common;

import net.minecraft.world.level.block.RedStoneOreBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RedStoneOreBlock.class)
public class RedStoneOreBlockMixin {
	@Inject(method = "interact", at = @At("HEAD"), cancellable = true)
	private static void vl$disableInteraction(CallbackInfo ci) {
		ci.cancel();
	}
}
