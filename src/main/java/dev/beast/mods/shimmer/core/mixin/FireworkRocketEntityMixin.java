package dev.beast.mods.shimmer.core.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireworkRocketEntity.class)
public class FireworkRocketEntityMixin {
	@Inject(at = @At("HEAD"), method = "explode")
	public void shimmer$killRidingPlayer(CallbackInfo ci) {
		for (var entity : (((FireworkRocketEntity) (Object) this)).getPassengers()) {
			entity.kill();
		}
	}

	@Inject(at = @At("HEAD"), method = "tick")
	public void shimmer$noclip(CallbackInfo ci) {
		((Entity) (Object) this).noPhysics = true;
	}
}
