package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.feature.camera.ControlledCameraOverride;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	@ModifyConstant(method = "onScroll", constant = @Constant(floatValue = 0.2F))
	private float shimmer$maxFlySpeed(float constant) {
		return 3F;
	}

	@Redirect(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
	private void shimmer$turnPlayer(LocalPlayer instance, double yaw, double pitch) {
		if (!(instance.shimmer$sessionData().cameraOverride instanceof ControlledCameraOverride c) || !c.turn(yaw, pitch)) {
			instance.turn(yaw, pitch);
		}
	}
}
