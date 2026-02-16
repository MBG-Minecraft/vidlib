package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
	@Inject(method = "tick", at = @At("RETURN"))
	private void vl$tick(CallbackInfo ci) {
		for (var button : ((TitleScreen) (Object) this).renderables) {
			if (button.getClass().getName().equals("com.moulberry.flashback.screen.FlashbackButton")) {
				((AbstractWidget) button).active = ClientGameEngine.INSTANCE.enableReplayMainMenuButton();
			}
		}
	}

	@ModifyExpressionValue(method = "createNormalMenuOptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/TitleScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", ordinal = 0))
	private GuiEventListener vl$perspectiveOn(GuiEventListener original) {
		((AbstractWidget) original).active = ClientGameEngine.INSTANCE.enableSinglePlayerMainMenuButton();
		return original;
	}
}
