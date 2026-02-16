package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.NeutralMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NeutralMob.class)
public interface NeutralMobMixin {
	@Inject(method = "isAngryAtAllPlayers", at = @At("HEAD"), cancellable = true)
	default void vl$isAngryAtAllPlayers(ServerLevel level, CallbackInfoReturnable<Boolean> cir) {
		if (CommonGameEngine.INSTANCE.isAngryByDefault(level, (NeutralMob) this)) {
			cir.setReturnValue(true);
		}
	}
}
