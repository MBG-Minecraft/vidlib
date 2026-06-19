package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = DebugScreenOverlay.class, priority = 1337) // Ensure this mixin runs after others
public abstract class DebugScreenOverlayMixin {
	@Final
	@Shadow
	private Minecraft minecraft;

	@ModifyArg(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;extractLines(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Ljava/util/List;Z)V", ordinal = 0), index = 1)
	private List<String> collectGameInformationText(List<String> original) {
		var list = ClientGameEngine.INSTANCE.collectGameInformationText(minecraft, (DebugScreenOverlay) (Object) this);
		return list == null ? original : list;
	}

	@ModifyArg(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;extractLines(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Ljava/util/List;Z)V", ordinal = 1), index = 1)
	private List<String> getSystemInformation(List<String> original) {
		return Minecraft.getInstance().showOnlyReducedInfo() ? List.of() : original;
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
