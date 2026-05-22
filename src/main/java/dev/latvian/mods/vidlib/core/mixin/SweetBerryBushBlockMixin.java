package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SweetBerryBushBlock.class)
public class SweetBerryBushBlockMixin {
	@Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
	protected void vl$entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier p_405414_, CallbackInfo ci) {
		if (CommonGameEngine.INSTANCE.disablePricklyBerryBushes(level, pos, state, entity)) {
			ci.cancel();
		}
	}
}
