package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.latvian.mods.vidlib.feature.client.SleepScreen;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 1.21.5 hooked both of these through {@code Minecraft}: the social-interactions screen was
 * suppressed from {@code Minecraft.handleKeybinds} (via {@code Minecraft.setScreen}), and the
 * in-bed chat screen was wrapped in {@code Minecraft.tick} (via the {@code InBedChatScreen}
 * constructor). 26.3 moved both methods onto {@link Gui} itself, so they now live here.
 */
@Mixin(Gui.class)
public class GuiMixin {
	@Redirect(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", ordinal = 1))
	private void vl$openSocialInteractionsScreen(Gui instance, Screen old) {
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;openScreen(Lnet/minecraft/client/gui/components/ChatComponent$ChatMethod;Lnet/minecraft/client/gui/screens/ChatScreen$ChatConstructor;)V"))
	private void vl$sleepScreen(ChatComponent instance, ChatComponent.ChatMethod method, ChatScreen.ChatConstructor<?> constructor, Operation<Void> original) {
		if (ClientGameEngine.INSTANCE.removeChatFromSleepScreen()) {
			original.call(instance, method, (ChatScreen.ChatConstructor<ChatScreen>) (prefix, secure) -> new SleepScreen());
		} else {
			original.call(instance, method, constructor);
		}
	}
}
