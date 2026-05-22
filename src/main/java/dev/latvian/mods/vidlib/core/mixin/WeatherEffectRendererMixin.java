package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WeatherEffectRenderer.class)
public class WeatherEffectRendererMixin {
	@Unique
	private Biome.Precipitation vl$globalOverride = null;

	@Inject(method = "render(Lnet/minecraft/world/level/Level;Lnet/minecraft/client/renderer/MultiBufferSource;IFLnet/minecraft/world/phys/Vec3;)V", at = @At("HEAD"))
	private void vl$render(Level level, MultiBufferSource bufferSource, int ticks, float partialTick, Vec3 cameraPosition, CallbackInfo ci) {
		vl$globalOverride = ClientGameEngine.INSTANCE.overrideGlobalVisualPrecipitation((ClientLevel) level, partialTick, cameraPosition);
	}

	@Inject(method = "getPrecipitationAt", at = @At("HEAD"), cancellable = true)
	private void vl$collectRain(Level level, BlockPos pos, CallbackInfoReturnable<Biome.Precipitation> cir) {
		if (vl$globalOverride != null) {
			cir.setReturnValue(vl$globalOverride);
		}
	}
}
