package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.resources.sounds.BiomeAmbientSoundsHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BiomeAmbientSoundsHandler.class)
public class BiomeAmbientSoundsHandlerMixin {
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	public void vl$tick(CallbackInfo ci) {
		if (ClientGameEngine.INSTANCE.overrideBiomeMoodSounds((BiomeAmbientSoundsHandler) (Object) this)) {
			ci.cancel();
		}
	}
}
