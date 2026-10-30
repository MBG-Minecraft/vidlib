package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.gui.components.Button;
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

	/**
	 * 26.3 renamed {@code addFeedbackButtons} to {@code createPauseMenu} and moved the
	 * feedback/bug-report row onto a {@code LinearLayout} instead of the {@code GridLayout.RowHelper}.
	 */
	@Inject(method = "createPauseMenu", at = @At("HEAD"))
	private void vl$addFeedbackButtonsHead(CallbackInfo ci, @Share("buttonList") LocalRef<List<Button>> buttonList) {
		buttonList.set(new ArrayList<>());
	}

	@ModifyExpressionValue(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/LinearLayout;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;"))
	private static <T extends LayoutElement> T vl$addButton(T original, @Share("buttonList") LocalRef<List<Button>> buttonList) {
		if (original instanceof Button button) {
			buttonList.get().add(button);
		}

		return original;
	}

	@Inject(method = "createPauseMenu", at = @At("RETURN"))
	private void vl$addFeedbackButtonsReturn(CallbackInfo ci, @Share("buttonList") LocalRef<List<Button>> buttonList) {
		for (var button : buttonList.get()) {
			button.active = false;
		}
	}
}
