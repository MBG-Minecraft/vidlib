package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.feature.misc.MiscShimmerClientUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	protected abstract boolean handleChunkDebugKeys(int keyCode);

	@ModifyConstant(method = "keyPress", constant = @Constant(intValue = GLFW.GLFW_KEY_B))
	private int shimmer$keyPress$modifyConst(int keyB, long windowHandle, int key, int scancode, int action, int mods) {
		return -100000;
	}

	@Redirect(method = "keyPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;hasControlDown()Z", ordinal = 1))
	private boolean shimmer$keyPress$redirHasControlDown() {
		return false;
	}

	@Inject(method = "handleDebugKeys", at = @At("HEAD"), cancellable = true)
	private void shimmer$handleDebugKeysPre(int key, CallbackInfoReturnable<Boolean> cir) {
		if (MiscShimmerClientUtils.handleDebugKeys(minecraft, key)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "handleDebugKeys", at = @At("RETURN"), cancellable = true)
	private void shimmer$handleDebugKeysPost(int key, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValueZ()) {
			cir.setReturnValue(handleChunkDebugKeys(key));
		}
	}

	/**
	 * @author Lat
	 * @reason Move debug message to status
	 */
	@Overwrite
	private void debugComponent(ChatFormatting formatting, Component message) {
		minecraft.gui.setOverlayMessage(Component.empty().append(Component.translatable("debug.prefix").withStyle(formatting, ChatFormatting.BOLD)).append(CommonComponents.SPACE).append(message), false);
	}
}