package dev.latvian.mods.vidlib.core.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.vidlib.core.api.VLBridge;
import dev.latvian.mods.vidlib.feature.entity.VLVehicle;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@ModifyReturnValue(method = "collectAllColliders", at = @At("RETURN"))
	private static List<VoxelShape> vl$collectColliders(List<VoxelShape> parent, @Local(argsOnly = true) @Nullable Entity entity, @Local(argsOnly = true) Level level, @Local(argsOnly = true) AABB collisionBox) {
		var list = new ArrayList<VoxelShape>(0);
		VLBridge.collectShapesIntersecting.apply(level, entity, collisionBox, list);

		if (parent.isEmpty()) {
			return list;
		} else if (!list.isEmpty()) {
			list.addAll(0, parent);
			return list;
		} else {
			return parent;
		}
	}

	@ModifyReturnValue(method = "getGravity", at = @At("RETURN"))
	private double vl$getGravity(double original) {
		return original * CommonGameEngine.INSTANCE.getGravityModifier((Entity) (Object) this);
	}

	@Inject(method = "addPassenger", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList;copyOf(Ljava/util/Collection;)Lcom/google/common/collect/ImmutableList;"))
	private void vl$addPassenger(Entity passenger, CallbackInfo ci, @Local List<Entity> list) {
		if (this instanceof VLVehicle vehicle) {
			vehicle.sortPassengers(list);
		}
	}
}
