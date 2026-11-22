package dev.latvian.mods.vidlib.core.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.latvian.mods.vidlib.core.api.VLBridge;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mob.class)
public abstract class MobMixin {
	@ModifyReturnValue(method = "getAttackBoundingBox", at = @At("RETURN"))
	private AABB vl$getAttackBoundingBox(AABB original) {
		var mob = (Mob) (Object) this;
		return VLBridge.getAttackBoundingBox.apply(mob, original);
	}
}
