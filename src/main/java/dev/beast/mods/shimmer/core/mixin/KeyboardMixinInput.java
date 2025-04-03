package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.feature.camera.ControlledCameraOverride;
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
		var in = (KeyboardInput) (Object) this;

		if (player != null && player.shimmer$sessionData().cameraOverride instanceof ControlledCameraOverride c && c.move(in)) {
			in.keyPresses = Input.EMPTY;
			in.forwardImpulse = 0F;
			in.leftImpulse = 0F;
		} else if (player != null && player.shimmer$sessionData().suspended) {
			in.keyPresses = in.keyPresses.shift() ? new Input(
				false,
				false,
				false,
				false,
				false,
				true,
				false
			) : Input.EMPTY;

			in.forwardImpulse = 0F;
			in.leftImpulse = 0F;
		}
	}
}
