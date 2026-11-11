package dev.latvian.mods.vidlib.core.mixin.common;

import com.google.common.collect.Iterables;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.vidlib.feature.session.LevelSessionData;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
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

import java.util.ArrayList;

@Mixin(CollisionGetter.class)
public interface CollisionGetterMixin {
	@Inject(method = "noCollision(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Z", at = @At("HEAD"), cancellable = true)
	default void vl$noCollision(@Nullable Entity entity, AABB collisionBox, CallbackInfoReturnable<Boolean> cir) {
		if (this instanceof Level level && LevelSessionData.of(level).intersectsSolid(entity, collisionBox)) {
			cir.setReturnValue(false);
		}
	}

	@ModifyReturnValue(method = "getCollisions", at = @At("RETURN"))
	default Iterable<VoxelShape> vl$getCollisions(Iterable<VoxelShape> parent, @Local(argsOnly = true) @Nullable Entity entity, @Local(argsOnly = true) AABB collisionBox) {
		var shapes = new ArrayList<VoxelShape>(0);
		CommonGameEngine.INSTANCE.collectShapesIntersecting((CollisionGetter) this, entity, collisionBox, shapes);
		return shapes.isEmpty() ? parent : Iterables.concat(parent, shapes);
	}
}
