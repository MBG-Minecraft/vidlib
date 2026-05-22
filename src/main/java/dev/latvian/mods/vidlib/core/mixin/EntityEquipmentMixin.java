package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumMap;

@Mixin(EntityEquipment.class)
public class EntityEquipmentMixin {
	@Shadow
	@Final
	private EnumMap<EquipmentSlot, ItemStack> items;

	@Inject(method = "tick", at = @At("RETURN"))
	private void vl$tick(Entity entity, CallbackInfo ci) {
		for (var entry : items.entrySet()) {
			var stack = entry.getValue();

			if (!stack.isEmpty()) {
				var updated = CommonGameEngine.INSTANCE.itemInventoryTick(stack, entity, entry.getKey());

				if (stack != updated || updated.isEmpty()) {
					entry.setValue(updated.isEmpty() ? ItemStack.EMPTY : updated);
				}
			}
		}
	}

	@Nullable
	@ModifyExpressionValue(method = "dropAll", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;"))
	private ItemEntity vl$drop(@Nullable ItemEntity entity, @Local(argsOnly = true) LivingEntity parent) {
		if (entity != null) {
			CommonGameEngine.INSTANCE.modifyDroppedItem(parent, entity);
		}

		return entity;
	}
}
