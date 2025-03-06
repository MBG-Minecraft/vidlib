package dev.beast.mods.shimmer.core.mixin;

import com.google.common.collect.Iterables;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CollisionGetter.class)
public interface CollisionGetterMixin {
	@Inject(method = "noCollision(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Z", at = @At("HEAD"), cancellable = true)
	default void shimmer$noCollision(@Nullable Entity entity, AABB collisionBox, CallbackInfoReturnable<Boolean> cir) {
		var zones = this instanceof Level level ? level.shimmer$getActiveZones() : null;

		if (zones != null && zones.intersectsSolid(entity, collisionBox)) {
			cir.setReturnValue(false);
		}
	}

	@ModifyReturnValue(method = "getCollisions", at = @At("RETURN"))
	default Iterable<VoxelShape> shimmer$getCollisions(Iterable<VoxelShape> parent, @Local(argsOnly = true) @Nullable Entity entity, @Local(argsOnly = true) AABB collisionBox) {
		var zones = this instanceof Level level ? level.shimmer$getActiveZones() : null;

		if (zones != null) {
			var list = zones.getShapesIntersecting(entity, collisionBox);

			if (!list.isEmpty()) {
				return Iterables.concat(list, parent);
			}
		}

		return parent;
	}
}
