package dev.latvian.mods.vidlib.core.mixin;

import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Mob.class)
public abstract class MobMixin {
	/*
	@Inject(method = "isNoAi", at = @At("HEAD"), cancellable = true)
	private void vl$isNoAi(CallbackInfoReturnable<Boolean> cir) {
		var override = EntityOverride.AI.get((Mob) (Object) this);

		if (override != null) {
			cir.setReturnValue(override);
		}
	}
	 */
}
