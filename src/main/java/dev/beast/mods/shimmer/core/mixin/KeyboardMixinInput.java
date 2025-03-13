package dev.beast.mods.shimmer.core.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardMixinInput {
	@Inject(method = "tick", at = @At("RETURN"))
	private void shimmer$tick(CallbackInfo ci) {
		var player = Minecraft.getInstance().player;

		if (player != null && player.shimmer$sessionData().suspended) {
			var in = (KeyboardInput) (Object) this;

			in.keyPresses = new Input(
				false,
				false,
				false,
				false,
				false,
				in.keyPresses.shift(),
				false
			);

			in.forwardImpulse = 0F;
			in.leftImpulse = 0F;
		}
	}
}
