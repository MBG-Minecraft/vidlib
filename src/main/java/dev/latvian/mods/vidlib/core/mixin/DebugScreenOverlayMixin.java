package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * 26.3 replaced {@code DebugScreenOverlay.getSystemInformation()} /
 * {@code collectGameInformationText()} with the entry-driven
 * {@link DebugScreenOverlay#extractRenderState(GuiGraphicsExtractor)}, so the reduced-info
 * handling now cancels the vanilla extraction and emits VidLib's own line list directly.
 */
@Mixin(value = DebugScreenOverlay.class, priority = 1337) // Ensure this mixin runs after others
public abstract class DebugScreenOverlayMixin {
	@Final
	@Shadow
	private Minecraft minecraft;

	@Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
	private void vl$extractRenderState(GuiGraphicsExtractor graphics, CallbackInfo ci) {
		List<String> list = ClientGameEngine.INSTANCE.collectGameInformationText(minecraft, (DebugScreenOverlay) (Object) this);

		if (list == null) {
			return;
		}

		graphics.nextStratum();
		graphics.pose().pushMatrix();
		graphics.pose().translate(2.0F, 2.0F);

		for (int i = 0; i < list.size(); i++) {
			graphics.text(minecraft.font, list.get(i), 0, i * 9, 0xFFE0E0E0, false);
		}

		graphics.pose().popMatrix();
		ci.cancel();
	}

	/**
	 * Replaces NeoForge 1.21.5's {@code CustomizeGuiOverlayEvent.DebugText}, which no longer exists
	 * in 26.3: the debug overlay is entry-driven, but the left/right line lists are still built
	 * locally inside {@code extractRenderState}. They are captured here, right after the last
	 * vanilla entry has been displayed (the first {@code List.isEmpty()} check that follows).
	 */
	@Inject(
		method = "extractRenderState",
		at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", ordinal = 0)
	)
	private void vl$debugText(GuiGraphicsExtractor graphics, CallbackInfo ci, @Local(ordinal = 0) List<String> leftLines, @Local(ordinal = 1) List<String> rightLines) {
		dev.latvian.mods.vidlib.VidLibClientEventHandler.debugText(leftLines, rightLines);
	}

	@Inject(method = "showNetworkCharts", at = @At("RETURN"), cancellable = true)
	public void showNetworkCharts(CallbackInfoReturnable<Boolean> cir) {
		if (ClientGameEngine.INSTANCE.hideDebugCharts()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "showProfilerChart", at = @At("RETURN"), cancellable = true)
	public void showProfilerChart(CallbackInfoReturnable<Boolean> cir) {
		if (ClientGameEngine.INSTANCE.hideDebugCharts()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "showFpsCharts", at = @At("RETURN"), cancellable = true)
	public void showFpsCharts(CallbackInfoReturnable<Boolean> cir) {
		if (ClientGameEngine.INSTANCE.hideDebugCharts()) {
			cir.setReturnValue(false);
		}
	}
}
