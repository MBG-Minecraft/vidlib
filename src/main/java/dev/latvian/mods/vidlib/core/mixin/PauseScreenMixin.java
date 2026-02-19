package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin {
	@Shadow
	@Final
	private static Component PLAYER_REPORTING;

	@ModifyReturnValue(method = "openScreenButton", at = @At("RETURN"))
	private Button vl$openScreenButton(Button original, @Local(argsOnly = true) Component c) {
		if (c == PLAYER_REPORTING) {
			original.active = false;
		}

		return original;
	}

	@Inject(method = "addFeedbackButtons", at = @At("HEAD"))
	private static void vl$addFeedbackButtonsHead(Screen lastScreen, GridLayout.RowHelper rowHelper, CallbackInfo ci, @Share("buttonList") LocalRef<List<Button>> buttonList) {
		buttonList.set(new ArrayList<>());
	}

	@ModifyExpressionValue(method = "addFeedbackButtons", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;"))
	private static <T extends LayoutElement> T vl$addButton(T original, @Share("buttonList") LocalRef<List<Button>> buttonList) {
		buttonList.get().add((Button) original);
		return original;
	}

	@Inject(method = "addFeedbackButtons", at = @At("RETURN"))
	private static void vl$addFeedbackButtonsReturn(Screen lastScreen, GridLayout.RowHelper rowHelper, CallbackInfo ci, @Share("buttonList") LocalRef<List<Button>> buttonList) {
		for (var button : buttonList.get()) {
			button.active = false;
		}
	}
}
