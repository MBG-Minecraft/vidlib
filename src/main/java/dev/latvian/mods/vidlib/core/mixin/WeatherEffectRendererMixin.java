package dev.latvian.mods.vidlib.core.mixin;

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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.3 turned {@link WeatherEffectRenderer} into an extract-then-render renderer:
 * {@code render(Level, MultiBufferSource, int, float, Vec3)} and {@code getPrecipitationAt} are
 * gone, replaced by {@code extractRenderState(ClientLevel, float, Vec3, WeatherRenderState)} —
 * which samples {@code Level.precipitationAt(BlockPos)} per column — and
 * {@code render(WeatherRenderState, RenderPass)}.
 */
@Mixin(WeatherEffectRenderer.class)
public class WeatherEffectRendererMixin {
	@Unique
	private Biome.Precipitation vl$globalOverride = null;

	@Inject(method = "extractRenderState", at = @At("HEAD"))
	private void vl$extractRenderState(ClientLevel level, float partialTicks, Vec3 cameraPos, WeatherRenderState renderState, CallbackInfo ci) {
		vl$globalOverride = ClientGameEngine.INSTANCE.overrideGlobalVisualPrecipitation(level, partialTicks, cameraPos);
	}

	/**
	 * Forces the global precipitation override wherever vanilla would not spawn any weather.
	 */
	@Redirect(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getPrecipitationAt(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome$Precipitation;"))
	private Biome.Precipitation vl$precipitationAt(ClientLevel level, BlockPos pos) {
		Biome.Precipitation precipitation = level.getPrecipitationAt(pos);
		return precipitation == Biome.Precipitation.NONE && vl$globalOverride != null ? vl$globalOverride : precipitation;
	}
}
