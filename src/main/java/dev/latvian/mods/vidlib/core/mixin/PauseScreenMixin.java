package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.latvian.mods.vidlib.feature.misc.MainMenuOpenedEvent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
	@Shadow
	@Final
	private static Component PLAYER_REPORTING;

	protected PauseScreenMixin(Component title) {
		super(title);
	}

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

	@Inject(method = "onDisconnect", at = @At("RETURN"))
	private void vl$onDisconnect(CallbackInfo ci) {
		NeoForge.EVENT_BUS.post(new MainMenuOpenedEvent(minecraft, false));
	}

	/*
	@Inject(method = "renderBackground", at = @At("RETURN"))
	private void vl$renderBackground(GuiGraphics graphics, int mx, int my, float delta, CallbackInfo ci) {
		graphics.fill(0, 0, width, height, 0xFF000000);
	}
	 */
}
