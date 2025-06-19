package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLMouseHandler;
import dev.latvian.mods.vidlib.feature.camera.ControlledCameraOverride;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin implements VLMouseHandler {
	@Shadow
	private boolean isLeftPressed;

	@Shadow
	private boolean isRightPressed;

	@Shadow
	private boolean isMiddlePressed;

	@Shadow
	private int activeButton;

	@Shadow
	private double ypos;

	@Shadow
	private double xpos;

	@Shadow
	@Final
	private ScrollWheelHandler scrollWheelHandler;

	@ModifyConstant(method = "onScroll", constant = @Constant(floatValue = 0.2F))
	private float vl$maxFlySpeed(float constant) {
		return 3F;
	}

	@Redirect(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
	private void vl$turnPlayer(LocalPlayer instance, double yaw, double pitch) {
		if (!(instance.vl$sessionData().cameraOverride instanceof ControlledCameraOverride c) || !c.turn(yaw, pitch)) {
			instance.turn(yaw, pitch);
		}
	}

	@Inject(method = "onPress", at = @At("HEAD"), cancellable = true)
	public void vl$onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
		if (ImGuiHooks.shouldInterceptMouse()) {
			ci.cancel();
		}
	}

	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	public void vl$onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
		if (ImGuiHooks.shouldInterceptMouse()) {
			ci.cancel();
		}
	}

	@ModifyVariable(method = "onMove", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	public double vl$modifyCursorX(double x) {
		return Minecraft.getInstance().getWindow().vl$modifyCursorX(x);
	}

	@ModifyVariable(method = "onMove", at = @At("HEAD"), ordinal = 1, argsOnly = true)
	public double vl$modifyCursorY(double y) {
		return Minecraft.getInstance().getWindow().vl$modifyCursorY(y);
	}

	@Override
	public void vl$resetMouse() {
		isLeftPressed = isRightPressed = isMiddlePressed = false;
		activeButton = -1;
		xpos = ypos = -1;
		// FIXME: scrollWheelHandler.accumulatedScrollX = scrollWheelHandler.accumulatedScrollY = 0;
	}
}
