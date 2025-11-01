package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.blaze3d.platform.Window;
import dev.latvian.mods.vidlib.core.VLWindow;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiHooks;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Window.class)
public class WindowMixin implements VLWindow {
	@Shadow
	private int framebufferWidth;

	@Shadow
	private int framebufferHeight;

	@Shadow
	private int width;

	@Shadow
	private int height;

	@Shadow
	private int windowedWidth;

	@Shadow
	private int windowedHeight;

	@Unique
	private double vl$xScale = 1;

	@Unique
	private double vl$yScale = 1;

	@Unique
	private double vl$xOffset = 0;

	@Unique
	private double vl$yOffset = 0;

	@Unique
	private int vl$unscaledFramebufferWidth = 1;

	@Unique
	private int vl$unscaledFramebufferHeight = 1;

	@Unique
	private int vl$unscaledWidth = 1;

	@Unique
	private int vl$unscaledHeight = 1;

	@Inject(method = "<init>", at = @At("RETURN"))
	public void onConstruct(CallbackInfo ci) {
		ImGuiHooks.trackDpiScale((Window) (Object) this);
		vl$unscaledFramebufferWidth = framebufferWidth;
		vl$unscaledFramebufferHeight = framebufferHeight;
		vl$unscaledWidth = width;
		vl$unscaledHeight = height;
	}

	@ModifyVariable(method = "setWidth", at = @At(value = "HEAD"), ordinal = 0, argsOnly = true)
	public int setFramebufferWidth(int width) {
		return vl$transformNewFramebufferWidth(width);
	}

	@ModifyVariable(method = "setHeight", at = @At(value = "HEAD"), ordinal = 0, argsOnly = true)
	public int setFramebufferHeight(int height) {
		return vl$transformNewFramebufferHeight(height);
	}

	@Inject(method = "refreshFramebufferSize", at = @At("RETURN"))
	public void updateFramebufferSize(CallbackInfo ci) {
		framebufferWidth = vl$transformNewFramebufferWidth(framebufferWidth);
		framebufferHeight = vl$transformNewFramebufferHeight(framebufferHeight);
	}

	@ModifyVariable(method = "onFramebufferResize", at = @At(value = "HEAD"), ordinal = 0, argsOnly = true)
	public int onFramebufferSizeChanged$width(int width) {
		return vl$transformNewFramebufferWidth(width);
	}

	@ModifyVariable(method = "onFramebufferResize", at = @At(value = "HEAD"), ordinal = 1, argsOnly = true)
	public int onFramebufferSizeChanged$height(int height) {
		return vl$transformNewFramebufferHeight(height);
	}

	@ModifyVariable(method = "onResize", at = @At(value = "HEAD"), ordinal = 0, argsOnly = true)
	public int onWindowSizeChanged$width(int width) {
		vl$unscaledWidth = width;
		return windowedWidth = (int) (vl$unscaledWidth * vl$xScale);
	}

	@ModifyVariable(method = "onResize", at = @At(value = "HEAD"), ordinal = 1, argsOnly = true)
	public int onWindowSizeChanged$height(int height) {
		vl$unscaledHeight = height;
		return windowedHeight = (int) (vl$unscaledHeight * vl$yScale);
	}

	@Inject(method = "close", at = @At("HEAD"))
	public void onDispose(CallbackInfo ci) {
		ImGuiHooks.dispose();
	}

	@Override
	public void vl$setViewportArea(double xOffset, double yOffset, double xScale, double yScale) {
		vl$xOffset = xOffset;
		vl$yOffset = yOffset;
		vl$xScale = xScale;
		vl$yScale = yScale;
		framebufferWidth = (int) (vl$unscaledFramebufferWidth * xScale);
		framebufferHeight = (int) (vl$unscaledFramebufferHeight * yScale);
		width = windowedWidth = (int) (vl$unscaledWidth * xScale);
		height = windowedHeight = (int) (vl$unscaledHeight * yScale);
	}

	@Override
	public double vl$getXOffset() {
		return vl$xOffset;
	}

	@Override
	public double vl$getYOffset() {
		return vl$yOffset;
	}

	@Override
	public double vl$getInverseYOffset() {
		return (1D - vl$yScale) - vl$yOffset;
	}

	@Override
	public int vl$getUnscaledWidth() {
		return vl$unscaledWidth;
	}

	@Override
	public int vl$getUnscaledHeight() {
		return vl$unscaledHeight;
	}

	@Override
	public int vl$getUnscaledFramebufferWidth() {
		return vl$unscaledFramebufferWidth;
	}

	@Override
	public int vl$getUnscaledFramebufferHeight() {
		return vl$unscaledFramebufferHeight;
	}

	@Unique
	private int vl$transformNewFramebufferWidth(int width) {
		vl$unscaledFramebufferWidth = width;
		return (int) (width * vl$xScale);
	}

	@Unique
	private int vl$transformNewFramebufferHeight(int height) {
		vl$unscaledFramebufferHeight = height;
		return (int) (height * vl$yScale);
	}

	@Inject(method = "calculateScale", at = @At("HEAD"), cancellable = true)
	private void vl$calculateScale(int guiScale, boolean forceUnicode, CallbackInfoReturnable<Integer> cir) {
		int s = ClientGameEngine.INSTANCE.calculateScale(framebufferWidth, framebufferHeight);

		if (s != -1) {
			cir.setReturnValue(s);
		}
	}
}
