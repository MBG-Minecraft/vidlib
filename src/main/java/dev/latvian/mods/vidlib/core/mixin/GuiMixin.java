package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Redirect(method = "renderTabList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isLocalServer()Z"))
	private boolean vl$renderTabList(Minecraft instance) {
		return false;
	}

	@Inject(method = "renderCameraOverlays", at = @At("HEAD"))
	private void vl$renderCameraOverlays(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		ClientGameEngine.INSTANCE.renderOverlays(minecraft, (Gui) (Object) this, graphics, deltaTracker);
	}

	/**
	 * @author Lat
	 * @reason Yeet
	 */
	@Overwrite
	protected void renderSpyglassOverlay(GuiGraphics guiGraphics, float scopeScale) {
	}

	@Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
	private void vl$renderCrosshair(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (ClientGameEngine.INSTANCE.hideGui(minecraft)) {
			ci.cancel();
		}
	}

	@ModifyExpressionValue(method = {"lambda$new$8", "lambda$new$9"}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;hideGui:Z"))
	private static boolean vl$hideGui(boolean original, @Local(argsOnly = true) Minecraft mc) {
		return ClientGameEngine.INSTANCE.hideGui(mc);
	}
}
