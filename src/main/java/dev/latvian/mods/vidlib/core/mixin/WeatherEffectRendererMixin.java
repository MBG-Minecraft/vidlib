package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.misc.CameraOverride;
import net.minecraft.client.Minecraft;
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
	private Biome.Precipitation vl$override = null;

	@Inject(method = "render(Lnet/minecraft/world/level/Level;Lnet/minecraft/client/renderer/MultiBufferSource;IFLnet/minecraft/world/phys/Vec3;)V", at = @At("HEAD"))
	private void vl$render(Level level, MultiBufferSource bufferSource, int ticks, float partialTick, Vec3 cameraPosition, CallbackInfo ci) {
		var cam = CameraOverride.get(Minecraft.getInstance());
		vl$override = cam == null ? null : cam.getWeatherOverride();
	}

	@Inject(method = "getPrecipitationAt", at = @At("HEAD"), cancellable = true)
	private void vl$collectRain(Level level, BlockPos pos, CallbackInfoReturnable<Biome.Precipitation> cir) {
		if (vl$override != null) {
			cir.setReturnValue(vl$override);
		}
	}
}
