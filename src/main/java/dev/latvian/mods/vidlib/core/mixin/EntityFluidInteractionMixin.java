package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityFluidInteraction.class)
public class EntityFluidInteractionMixin {
	@Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BlockGetter;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"))
	private FluidState vl$getFluidState(BlockGetter level, BlockPos pos) {
		return level instanceof Level l ? CommonGameEngine.INSTANCE.overrideFluidState(l, pos) : level.getFluidState(pos);
	}

	@Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;getHeight(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"))
	private float vl$getFluidHeight(FluidState state, BlockGetter blockGetter, BlockPos pos) {
		return blockGetter instanceof Level l ? CommonGameEngine.INSTANCE.overrideFluidHeight(l, state, pos) : state.getHeight(blockGetter, pos);
	}
}
