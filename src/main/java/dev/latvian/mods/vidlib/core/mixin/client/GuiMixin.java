package dev.latvian.mods.vidlib.core.mixin.client;

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

@Mixin(Gui.class)
public class GuiMixin {
	@WrapOperation(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", ordinal = 1))
	private void vl$openSocialInteractionsScreen(Gui instance, Screen screen, Operation<Void> original) {
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
