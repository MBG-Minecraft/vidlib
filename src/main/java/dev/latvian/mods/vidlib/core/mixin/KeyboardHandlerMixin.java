package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.imgui.ImGuiHooks;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	protected abstract boolean handleChunkDebugKeys(KeyEvent event);

	@ModifyConstant(method = "keyPress", constant = @Constant(intValue = GLFW.GLFW_KEY_B))
	private int vl$keyPress$modifyConst(int keyB, long windowHandle, int action, KeyEvent event) {
		return -100000;
	}

	@Inject(method = "handleDebugKeys", at = @At("HEAD"), cancellable = true)
	private void vl$handleDebugKeysPre(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (ClientGameEngine.INSTANCE.handleDebugKeys(minecraft, event.key())) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "handleDebugKeys", at = @At("RETURN"), cancellable = true)
	private void vl$handleDebugKeysPost(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValueZ()) {
			cir.setReturnValue(handleChunkDebugKeys(event));
		}
	}

	/**
	 * @author Lat
	 * @reason Move debug message to status
	 */
	@Overwrite
	private void showDebugChat(Component message) {
		minecraft.gui.setOverlayMessage(message, false);
	}

	@Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
	public void vl$onKey(long window, int action, KeyEvent event, CallbackInfo ci) {
		if (ImGuiHooks.shouldInterceptKeyboard()) {
			ci.cancel();
		}
	}

	@Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
	public void vl$onChar(long window, CharacterEvent event, CallbackInfo ci) {
		if (ImGuiHooks.shouldInterceptKeyboard()) {
			ci.cancel();
		}
	}
}
