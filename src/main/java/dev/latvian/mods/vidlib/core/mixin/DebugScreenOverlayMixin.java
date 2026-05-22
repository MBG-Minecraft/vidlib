package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = DebugScreenOverlay.class, priority = 1337) // Ensure this mixin runs after others
public abstract class DebugScreenOverlayMixin {
	@Final
	@Shadow
	private Minecraft minecraft;

	@Inject(method = "getSystemInformation", at = @At("RETURN"), cancellable = true)
	private void getSystemInformation(CallbackInfoReturnable<List<String>> cir) {
		if (Minecraft.getInstance().showOnlyReducedInfo()) {
			cir.setReturnValue(new ArrayList<>());
		}
	}

	@Inject(method = "collectGameInformationText", at = @At("RETURN"), cancellable = true)
	private void collectGameInformationText(CallbackInfoReturnable<List<String>> cir) {
		var list = ClientGameEngine.INSTANCE.collectGameInformationText(minecraft, (DebugScreenOverlay) (Object) this);

		if (list != null) {
			cir.setReturnValue(list);
		}
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
