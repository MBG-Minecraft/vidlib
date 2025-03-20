package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.ShimmerConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
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
	protected abstract void renderTextureOverlay(GuiGraphics guiGraphics, ResourceLocation shaderLocation, float alpha);

	@Shadow
	@Final
	private static ResourceLocation POWDER_SNOW_OUTLINE_LOCATION;

	@Shadow
	@Final
	private Minecraft minecraft;

	@Redirect(method = "renderTabList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isLocalServer()Z"))
	private boolean shimmer$renderTabList(Minecraft instance) {
		return false;
	}

	@Inject(method = "renderCameraOverlays", at = @At("HEAD"))
	private void shimmer$renderCameraOverlays(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (ShimmerConfig.renderSuspendedOverlay && minecraft.player.shimmer$sessionData().suspended) {
			renderTextureOverlay(graphics, POWDER_SNOW_OUTLINE_LOCATION, 1F);
		}
	}

	/**
	 * @author Lat
	 * @reason Yeet
	 */
	@Overwrite
	protected void renderSpyglassOverlay(GuiGraphics guiGraphics, float scopeScale) {
	}
}
