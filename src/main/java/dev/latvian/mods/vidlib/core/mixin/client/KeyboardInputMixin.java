package dev.latvian.mods.vidlib.core.mixin.client;

import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {
	@Inject(method = "tick", at = @At("RETURN"))
	private void vl$tick(CallbackInfo ci) {
		var mc = Minecraft.getInstance();
		var player = mc.player;

		if (player != null) {
			ClientGameEngine.INSTANCE.tickPlayerInput(mc, player, (KeyboardInput) (Object) this);
		}
	}
}
