package dev.latvian.mods.vidlib.core.mixin;

import com.google.common.collect.Iterables;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.vidlib.core.VLLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(CollisionGetter.class)
public interface CollisionGetterMixin {
	@Inject(method = "noCollision(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Z", at = @At("HEAD"), cancellable = true)
	default void vl$noCollision(@Nullable Entity entity, AABB collisionBox, CallbackInfoReturnable<Boolean> cir) {
		if (this instanceof VLLevel level && level.vl$intersectsSolid(entity, collisionBox)) {
			cir.setReturnValue(false);
		}
	}

	@ModifyReturnValue(method = "getCollisions", at = @At("RETURN"))
	default Iterable<VoxelShape> vl$getCollisions(Iterable<VoxelShape> parent, @Local(argsOnly = true) @Nullable Entity entity, @Local(argsOnly = true) AABB collisionBox) {
		var list = this instanceof VLLevel level ? level.vl$getShapesIntersecting(entity, collisionBox) : List.<VoxelShape>of();
		return list.isEmpty() ? parent : Iterables.concat(parent, list);
	}
}
