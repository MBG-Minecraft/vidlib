package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerLivingEntity;
import dev.beast.mods.shimmer.feature.entity.EntityOverride;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements ShimmerLivingEntity {
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
	private void shimmer$tick(CallbackInfo ci) {
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

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;aiStep()V"))
	private void shimmer$ai(LivingEntity instance) {
		if (!EntityOverride.SUSPENDED.get(instance, false)) {
			instance.aiStep();
		}
	}
}
