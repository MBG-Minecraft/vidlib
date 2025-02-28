package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.feature.entity.EntityOverride;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobMixin {
	@Inject(method = "isNoAi", at = @At("HEAD"), cancellable = true)
	private void shimmer$isNoAi(CallbackInfoReturnable<Boolean> cir) {
		var override = EntityOverride.AI.get((Mob) (Object) this);

		if (override != null) {
			cir.setReturnValue(override);
		}
	}
}
