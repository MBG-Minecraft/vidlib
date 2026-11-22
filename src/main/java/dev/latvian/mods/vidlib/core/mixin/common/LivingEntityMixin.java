package dev.latvian.mods.vidlib.core.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.vidlib.core.api.VLBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Shadow
	public abstract ItemStack getMainHandItem();

	@Shadow
	public abstract ItemStack getOffhandItem();

	/**
	 * @author Lat
	 * @reason Optimization
	 */
	@Overwrite
	public boolean isHolding(Item item) {
		return getMainHandItem().is(item) || getOffhandItem().is(item);
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;aiStep()V"))
	private void vl$tick(CallbackInfo ci) {
		VLBridge.livingTick.accept((LivingEntity) (Object) this);
	}

	@ModifyReturnValue(method = "getSpeed", at = @At("RETURN"))
	private float vl$getSpeed(float original) {
		return original * VLBridge.getSpeedModifier.applyAsFloat((LivingEntity) (Object) this);
	}

	@ModifyReturnValue(method = "isTransmittingWaypoint", at = @At("RETURN"))
	private boolean vl$isTransmittingWaypoint(boolean original) {
		return original || VLBridge.isTrackedByLocatorBar.test((LivingEntity) (Object) this);
	}

	@Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
	private void vl$isPushable(CallbackInfoReturnable<Boolean> cir) {
		if (VLBridge.isUnpushable.test((LivingEntity) (Object) this)) {
			cir.setReturnValue(false);
		}
	}

	@Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"))
	private FluidState vl$getFluidState(Level level, BlockPos pos) {
		return VLBridge.overrideFluidState.apply(level, pos);
	}

	@ModifyExpressionValue(method = "causeFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;calculateFallDamage(DF)I"))
	private int vl$causeFallDamage(int original, @Local(argsOnly = true) double fallDistance, @Local(argsOnly = true) float damageMultiplier, @Local(argsOnly = true) DamageSource damageSource) {
		return VLBridge.calculateFallDamage.apply((LivingEntity) (Object) this, fallDistance, damageMultiplier, damageSource, original);
	}
}