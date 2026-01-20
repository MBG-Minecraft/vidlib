package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ProjectileWeaponItem.class)
public abstract class ProjectileWeaponItemMixin {
	@ModifyReturnValue(method = "getHeldProjectile", at = @At("RETURN"))
	private static ItemStack vl$skipAmmo(ItemStack original) {
		return CommonGameEngine.INSTANCE.getInfiniteArrows() && original.isEmpty() ? Items.ARROW.getDefaultInstance() : original;
	}
}
