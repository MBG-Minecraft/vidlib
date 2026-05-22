package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrb.class)
public class ExperienceOrbMixin {
	@Inject(method = "award", at = @At("HEAD"), cancellable = true)
	private static void vl$award(ServerLevel level, Vec3 pos, int amount, CallbackInfo ci) {
		if (CommonGameEngine.INSTANCE.disableXP(level)) {
			ci.cancel();
		}
	}
}
