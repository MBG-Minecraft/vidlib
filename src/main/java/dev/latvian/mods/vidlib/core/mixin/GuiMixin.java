package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Gui.class)
public abstract class GuiMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	@Nullable
	private Component overlayMessageString;

	@WrapOperation(method = "extractTabList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isLocalServer()Z"))
	private boolean vl$renderTabList(Minecraft instance, Operation<Boolean> original) {
		return false;
	}

	@Inject(method = "extractCameraOverlays", at = @At("HEAD"))
	private void vl$renderCameraOverlays(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		ClientGameEngine.INSTANCE.renderOverlays(minecraft, (Gui) (Object) this, graphics, deltaTracker);
	}

	@Inject(method = "extractSpyglassOverlay", at = @At("HEAD"), cancellable = true)
	private void vl$extractSpyglassOverlay(GuiGraphicsExtractor graphics, float scopeScale, CallbackInfo ci) {
		ci.cancel();
	}

	@Inject(method = "extractCrosshair", at = @At("HEAD"), cancellable = true)
	private void vl$renderCrosshair(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (ClientGameEngine.INSTANCE.hideCrosshair(minecraft)) {
			ci.cancel();
		}
	}

	@Inject(method = "extractOverlayMessage", at = @At("HEAD"), cancellable = true)
	private void vl$renderOverlayMessage(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (overlayMessageString != null && ClientGameEngine.INSTANCE.hideActionBarText(minecraft, overlayMessageString)) {
			ci.cancel();
		}
	}

	@ModifyExpressionValue(method = {"lambda$registerVanillaLayers$0", "lambda$registerVanillaLayers$8"}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;hideGui:Z"))
	private boolean vl$hideGui(boolean original) {
		return ClientGameEngine.INSTANCE.hideGui(minecraft);
	}

	@ModifyExpressionValue(method = "extractHearts", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelData;isHardcore()Z"))
	private boolean vl$isHardcore(boolean original) {
		return ClientGameEngine.INSTANCE.renderHardcoreHearts(original);
	}

	@ModifyExpressionValue(method = {
		"extractSelectedItemName(Lnet/minecraft/client/gui/GuiGraphicsExtractor;I)V",
		"lambda$registerVanillaLayers$1"
	}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;canHurtPlayer()Z"))
	private boolean vl$isSurvival(boolean original) {
		return original || ClientGameEngine.INSTANCE.renderSpectatedUI(minecraft);
	}

	@ModifyExpressionValue(method = {
		"extractCrosshair",
		"extractHotbar",
		"maybeExtractSelectedItemName",
		"maybeExtractSpectatorTooltip"
	}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;getPlayerMode()Lnet/minecraft/world/level/GameType;"))
	private GameType vl$getGameMode(GameType original) {
		return ClientGameEngine.INSTANCE.renderSpectatedUI(minecraft) ? GameType.SURVIVAL : original;
	}

	@ModifyExpressionValue(method = "maybeExtractSpectatorTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"))
	private boolean vl$isSpectator(boolean original) {
		if (ClientGameEngine.INSTANCE.renderSpectatedUI(minecraft)) {
			return false;
		}

		return original;
	}
}
