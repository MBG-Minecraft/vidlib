package dev.latvian.mods.vidlib.core.mixin.common;

import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 1.21.5 hooked {@code Entity.updateFluidOnEyes} / {@code updateFluidHeightAndDoFluidPushing}.
 * 26.3 moved the whole per-fluid block scan into {@link EntityFluidInteraction#update}, where
 * the level is only known as a {@link BlockGetter} (the actual owner is still the entity's
 * own {@link Level}). The VIDL fluid overrides are resolved through
 * {@link CommonGameEngine}, exactly as before.
 */
@Mixin(EntityFluidInteraction.class)
public abstract class EntityFluidInteractionMixin {
	@Redirect(method = "update(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Predicate;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BlockGetter;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"))
	private FluidState vl$getFluidState(BlockGetter blockGetter, BlockPos pos) {
		return blockGetter instanceof Level level ? CommonGameEngine.INSTANCE.overrideFluidState(level, pos) : blockGetter.getFluidState(pos);
	}
}
