package dev.latvian.mods.vidlib.core.mixin.client;

import dev.latvian.mods.vidlib.feature.imgui.ImGuiHooks;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	protected abstract boolean handleDebugKeys(KeyEvent event);

	@Shadow
	protected boolean handleChunkDebugKeys(KeyEvent event) {
		return false;
	}

	@ModifyConstant(method = "keyPress", constant = @Constant(intValue = 98))
	private int vl$keyPress$modifyConst(int keyB, long handle, int action, KeyEvent event) {
		return -100000;
	}

	@Redirect(method = "keyPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/input/KeyEvent;hasControlDownWithQuirk()Z", ordinal = 1))
	private boolean vl$keyPress$redirHasControlDown(KeyEvent event) {
		return false;
	}

	@Inject(method = "handleDebugKeys", at = @At("HEAD"), cancellable = true)
	private void vl$handleDebugKeysPre(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (ClientGameEngine.INSTANCE.handleDebugKeys(minecraft, event.shortcutKey())) {
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
	private void debugWarningComponent(Component component) {
		this.minecraft.gui.hud.setOverlayMessage(decorateDebugComponent(ChatFormatting.RED, component), false);
	}

	/**
	 * @author Lat
	 * @reason Move debug message to status
	 */
	@Overwrite
	private void debugFeedbackComponent(Component component) {
		this.minecraft.gui.hud.setOverlayMessage(decorateDebugComponent(ChatFormatting.YELLOW, component), false);
	}

	private static Component decorateDebugComponent(ChatFormatting formatting, Component component) {
		return Component.empty()
			.append(Component.translatable("debug.prefix").withStyle(formatting, ChatFormatting.BOLD))
			.append(CommonComponents.SPACE)
			.append(component);
	}

	@Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
	public void vl$onKey(long handle, int action, KeyEvent event, CallbackInfo ci) {
		if (ImGuiHooks.shouldInterceptKeyboard()) {
			ci.cancel();
		}
	}

	@Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
	public void vl$onChar(long handle, CharacterEvent event, CallbackInfo ci) {
		if (ImGuiHooks.shouldInterceptKeyboard()) {
			ci.cancel();
		}
	}
}
