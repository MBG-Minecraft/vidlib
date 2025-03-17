package dev.beast.mods.shimmer.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.beast.mods.shimmer.core.ShimmerEntity;
import dev.beast.mods.shimmer.feature.entity.EntityOverride;
import dev.beast.mods.shimmer.feature.entity.EntityOverrideValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

@Mixin(Entity.class)
public abstract class EntityMixin implements ShimmerEntity {
	@Shadow
	private Level level;

	@ModifyReturnValue(method = "collectColliders", at = @At("RETURN"))
	private static List<VoxelShape> shimmer$collectColliders(List<VoxelShape> parent, @Local(argsOnly = true) Level level, @Local(argsOnly = true) @Nullable Entity entity, @Local(argsOnly = true) AABB collisionBox) {
		var zones = level.shimmer$getActiveZones();

		if (zones != null) {
			var list = zones.getShapesIntersecting(entity, collisionBox);

			if (parent.isEmpty()) {
				return list;
			} else if (!list.isEmpty()) {
				var list2 = new ArrayList<VoxelShape>(parent.size() + list.size());
				list2.addAll(parent);
				list2.addAll(list);
				return list2;
			}
		}

		return parent;
	}

	@Unique
	private Map<EntityOverride<?>, EntityOverrideValue<?>> shimmer$overrides = null;

	@Unique
	private boolean shimmer$isSaving = false;

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	public <T> T shimmer$getDirectOverride(EntityOverride<T> override) {
		var v = shimmer$overrides == null ? null : (EntityOverrideValue<T>) shimmer$overrides.get(override);
		return v == null ? null : v.get((Entity) (Object) this);
	}

	@Override
	public <T> void shimmer$setDirectOverride(EntityOverride<T> override, @Nullable EntityOverrideValue<T> value) {
		if (value == null) {
			if (shimmer$overrides != null) {
				shimmer$overrides.remove(override);

				if (shimmer$overrides.isEmpty()) {
					shimmer$overrides = null;
				}
			}
		} else {
			if (shimmer$overrides == null) {
				shimmer$overrides = new IdentityHashMap<>(1);
			}

			shimmer$overrides.put(override, value);
		}
	}

	@Override
	public boolean shimmer$isSaving() {
		return shimmer$isSaving;
	}

	@Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
	private void shimmer$isCurrentlyGlowing(CallbackInfoReturnable<Boolean> cir) {
		var override = level == null || !level.isClientSide ? null : shimmer$glowingOverride();

		if (override != null) {
			cir.setReturnValue(override);
		}
	}

	@Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
	private void shimmer$getTeamColor(CallbackInfoReturnable<Integer> cir) {
		var override = shimmer$teamColorOverride();

		if (override != null) {
			cir.setReturnValue(override);
		}
	}

	@ModifyReturnValue(method = "getGravity", at = @At("RETURN"))
	private double shimmer$getGravity(double original) {
		return original * shimmer$gravityMod();
	}

	@Inject(method = "saveWithoutId", at = @At("HEAD"))
	private void shimmer$beforeSave(CompoundTag compound, CallbackInfoReturnable<CompoundTag> cir) {
		shimmer$isSaving = true;
	}

	@Inject(method = "saveWithoutId", at = @At("RETURN"))
	private void shimmer$afterSave(CompoundTag compound, CallbackInfoReturnable<CompoundTag> cir) {
		shimmer$isSaving = false;
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
}
