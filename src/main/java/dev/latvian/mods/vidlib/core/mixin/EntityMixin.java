package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.vidlib.core.VLEntity;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import dev.latvian.mods.vidlib.feature.session.LevelSessionData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin implements VLEntity {
	@ModifyReturnValue(method = "collectAllColliders", at = @At("RETURN"))
	private static List<VoxelShape> vl$collectColliders(List<VoxelShape> parent, @Local(argsOnly = true) @Nullable Entity entity, @Local(argsOnly = true) Level level, @Local(argsOnly = true) AABB collisionBox) {
		var list = new ArrayList<VoxelShape>(0);
		LevelSessionData.of(level).collectShapesIntersecting(entity, collisionBox, list);

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
		return original * CommonGameEngine.INSTANCE.getGravityModifier(vl$self());
	}

	/**
	 * @author Lat
	 * @reason Fix forever falling
	 */
	@Overwrite
	protected void applyGravity() {
		var entity = (Entity) (Object) this;
		double gravity = entity.getGravity();
		var delta = entity.getDeltaMovement();

		if (gravity != 0D) {
			entity.setDeltaMovement(delta.add(0D, -gravity, 0D));
		} else {
			entity.setDeltaMovement(new Vec3(delta.x, 0D, delta.z));
			entity.resetFallDistance();
		}
	}

	@Inject(method = "addPassenger", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList;copyOf(Ljava/util/Collection;)Lcom/google/common/collect/ImmutableList;"))
	private void vl$addPassenger(Entity passenger, CallbackInfo ci, @Local List<Entity> list) {
		sortPassengers(list);
	}
}
