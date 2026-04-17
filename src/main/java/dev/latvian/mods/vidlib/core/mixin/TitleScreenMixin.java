package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
	@Unique
	private AbstractWidget vl$spButton;

	@Unique
	private AbstractWidget vl$mpButton;

	@Inject(method = "tick", at = @At("RETURN"))
	private void vl$tick(CallbackInfo ci) {
		AbstractWidget vl$replaysButton = null;

		for (var button : ((TitleScreen) (Object) this).renderables) {
			if (button.getClass().getName().equals("com.moulberry.flashback.screen.FlashbackButton")) {
				vl$replaysButton = (AbstractWidget) button;
			}
		}

		if (vl$spButton != null) {
			vl$spButton.active = ClientGameEngine.INSTANCE.enableSinglePlayerMainMenuButton();
		}

		if (vl$mpButton != null) {
			vl$mpButton.active = ClientGameEngine.INSTANCE.enableMultiPlayerMainMenuButton();
		}

		if (vl$replaysButton != null) {
			vl$replaysButton.active = ClientGameEngine.INSTANCE.enableReplayMainMenuButton();
		}
	}

	@ModifyExpressionValue(method = "createNormalMenuOptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/TitleScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", ordinal = 0))
	private GuiEventListener vl$createSinglePlayerButton(GuiEventListener original) {
		vl$spButton = (AbstractWidget) original;
		return original;
	}

	@ModifyExpressionValue(method = "createNormalMenuOptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/TitleScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", ordinal = 1))
	private GuiEventListener vl$createMultiPlayerButton(GuiEventListener original) {
		vl$mpButton = (AbstractWidget) original;
		return original;
	}
}
