package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.feature.auto.AutoInit;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
	@Inject(method = "allChanged", at = @At("RETURN"))
	private void shimmer$allChanged(CallbackInfo ci) {
		AutoInit.Type.CHUNKS_RELOADED.invoke();
	}
}
