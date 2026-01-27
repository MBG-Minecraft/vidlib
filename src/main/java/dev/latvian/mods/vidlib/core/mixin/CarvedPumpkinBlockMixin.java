package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CarvedPumpkinBlock.class)
public class CarvedPumpkinBlockMixin {
	@Inject(method = "trySpawnGolem", at = @At("HEAD"), cancellable = true)
	private void vl$trySpawnGolem(Level level, BlockPos pos, CallbackInfo ci) {
		if (CommonGameEngine.INSTANCE.disableGolems(level, pos)) {
			ci.cancel();
		}
	}
}
