package dev.latvian.mods.vidlib.core.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.vidlib.feature.structure.GhostStructureCollision;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Entity.class)
public abstract class EntityGhostCollisionMixin {
	@ModifyReturnValue(method = "collectCollidersIgnoringWorldBorder(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/Level;Ljava/util/List;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;", at = @At("RETURN"))
	private static List<VoxelShape> vl$addGhostCollisionShapes(List<VoxelShape> original, @Local(argsOnly = true) AABB box) {
		return GhostStructureCollision.feed(box, original);
	}

	@ModifyReturnValue(method = "collectCollidersIgnoringWorldBorder(Lnet/minecraft/world/phys/shapes/CollisionContext;Lnet/minecraft/world/level/Level;Ljava/util/List;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;", at = @At("RETURN"))
	private static List<VoxelShape> vl$addGhostCollisionShapesWithContext(List<VoxelShape> original, @Local(argsOnly = true) AABB box) {
		return GhostStructureCollision.feed(box, original);
	}

	@Inject(method = "collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;", at = @At("HEAD"))
	private void vl$beginCollide(Vec3 movement, CallbackInfoReturnable<Vec3> cir) {
		GhostStructureCollision.beginMove();
	}

	@ModifyReturnValue(method = "collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;", at = @At("RETURN"))
	private Vec3 vl$applyGhostCollision(Vec3 result, @Local(argsOnly = true) Vec3 movement) {
		return GhostStructureCollision.applyFallback((Entity) (Object) this, movement, result);
	}
}