package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.latvian.mods.vidlib.core.VLLivingEntity;
import dev.latvian.mods.vidlib.feature.entity.EntityOverride;
import net.minecraft.core.BlockPos;
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
public abstract class LivingEntityMixin implements VLLivingEntity {
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
		var entity = (LivingEntity) (Object) this;

		if (!entity.level().isClientSide() && entity.getHealth() < entity.getMaxHealth()) {
			var regen = EntityOverride.REGENERATE.get(this);

			if (regen != null && regen >= 0) {
				if (regen == 0) {
					entity.heal();
				} else if (entity.tickCount % regen == 0) {
					entity.heal(1F);
				}
			}
		}
	}

	@ModifyReturnValue(method = "getSpeed", at = @At("RETURN"))
	private float vl$getSpeed(float original) {
		return original * vl$speedMod();
	}

	@Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
	private void vl$isPushable(CallbackInfoReturnable<Boolean> cir) {
		if (vl$unpushable()) {
			cir.setReturnValue(false);
		}
	}

	@Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"))
	private FluidState vl$getFluidState(Level level, BlockPos pos) {
		return level.vl$overrideFluidState(pos);
	}
}
