package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.VidLibConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.WaterFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WaterFluid.class)
public class WaterFluidMixin {
	@Inject(method = "animateTick", at = @At("HEAD"), cancellable = true)
	public void vl$animateTick(Level level, BlockPos pos, FluidState state, RandomSource random, CallbackInfo ci) {
		if (VidLibConfig.hideWaterParticles) {
			ci.cancel();
		}
	}
}
