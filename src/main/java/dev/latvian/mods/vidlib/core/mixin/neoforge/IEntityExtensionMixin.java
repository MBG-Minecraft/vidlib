package dev.latvian.mods.vidlib.core.mixin.neoforge;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.latvian.mods.vidlib.core.api.VLBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.common.extensions.IEntityExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(IEntityExtension.class)
public interface IEntityExtensionMixin {
	@WrapOperation(method = "lambda$canStartSwimming$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"))
	private static FluidState vl$getFluidState(Level level, BlockPos pos, Operation<FluidState> original) {
		return VLBridge.overrideFluidState.apply(level, pos);
	}
}
