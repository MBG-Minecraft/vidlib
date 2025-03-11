package dev.beast.mods.shimmer.core.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardMixinInput {
	@Inject(method = "tick", at = @At("RETURN"))
	private void shimmer$tick(boolean isSneaking, float sneakingSpeedMultiplier, CallbackInfo ci) {
		var player = Minecraft.getInstance().player;

		if (player != null && player.shimmer$sessionData().suspended) {
			var in = (KeyboardInput) (Object) this;
			in.up = false;
			in.down = false;
			in.left = false;
			in.right = false;
			in.jumping = false;
			in.forwardImpulse = 0F;
			in.leftImpulse = 0F;
		}
	}
}
