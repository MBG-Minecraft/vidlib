package dev.latvian.mods.vidlib.core.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.state.level.WeatherRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WeatherEffectRenderer.class)
public class WeatherEffectRendererMixin {
	@Unique
	private Biome.Precipitation vl$globalOverride = null;

	@Inject(method = "extractRenderState", at = @At("HEAD"))
	private void vl$extractRenderState(ClientLevel level, float partialTicks, Vec3 cameraPos, WeatherRenderState renderState, CallbackInfo ci) {
		vl$globalOverride = ClientGameEngine.INSTANCE.overrideGlobalVisualPrecipitation(level, partialTicks, cameraPos);
	}

	@WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getPrecipitationAt(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome$Precipitation;"))
	private Biome.Precipitation vl$precipitationAt(ClientLevel level, BlockPos pos, Operation<Biome.Precipitation> original) {
		var precipitation = level.getPrecipitationAt(pos);
		return precipitation == Biome.Precipitation.NONE && vl$globalOverride != null ? vl$globalOverride : precipitation;
	}
}
