package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.renderer.LevelEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelEventHandler.class)
public class LevelEventHandlerMixin {
	@Shadow
	@Final
	private Level level;

	@Inject(method = "levelEvent", at = @At("HEAD"), cancellable = true)
	private void vl$levelEvent(int eventId, BlockPos pos, int data, CallbackInfo ci) {
		if (ClientGameEngine.INSTANCE.overrideLevelEvent(level, eventId, pos, data)) {
			ci.cancel();
		}
	}
}
