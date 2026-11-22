package dev.latvian.mods.vidlib.core.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.vidlib.core.api.VLBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FluidState.class)
public abstract class FluidStateMixin {
	@ModifyReturnValue(method = "getHeight", at = @At("RETURN"))
	private float vl$getHeight(float original, @Local(argsOnly = true, name = "level") BlockGetter level, @Local(argsOnly = true, name = "pos") BlockPos pos) {
		if (level instanceof Level l) {
			return VLBridge.overrideFluidHeight.apply(l, (FluidState) (Object) this, pos, original);
		}

		return original;
	}
}
