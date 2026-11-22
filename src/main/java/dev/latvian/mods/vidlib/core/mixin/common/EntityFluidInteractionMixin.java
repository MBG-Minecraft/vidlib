package dev.latvian.mods.vidlib.core.mixin.common;

import dev.latvian.mods.vidlib.core.api.VLBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityFluidInteraction.class)
public abstract class EntityFluidInteractionMixin {
	@Redirect(method = "update(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Predicate;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BlockGetter;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"))
	private FluidState vl$getFluidState(BlockGetter blockGetter, BlockPos pos) {
		return blockGetter instanceof Level level ? VLBridge.overrideFluidState.apply(level, pos) : blockGetter.getFluidState(pos);
	}
}
