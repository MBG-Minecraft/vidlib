package dev.latvian.mods.vidlib.core.mixin.neo;

import dev.latvian.mods.vidlib.core.api.common.VLEntity;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.common.extensions.IEntityExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(IEntityExtension.class)
public interface IEntityExtensionMixin extends VLEntity {
	/**
	 * 1.21.5 hooked the {@code Level.getFluidState} call inside {@code canStartSwimming()}
	 * directly. 26.3 refactored that body into the synthetic lambda
	 * {@code lambda$canStartSwimming$0}, so the redirect now has to target the lambda - the
	 * logic being overridden is exactly the same.
	 */
	@Redirect(method = "lambda$canStartSwimming$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"))
	private static FluidState vl$getFluidState(Level level, BlockPos pos) {
		return CommonGameEngine.INSTANCE.overrideFluidState(level, pos);
	}
}
