package dev.latvian.mods.vidlib.core.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.latvian.mods.vidlib.core.api.VLBridge;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ProjectileWeaponItem.class)
public abstract class ProjectileWeaponItemMixin {
	@ModifyReturnValue(method = "getHeldProjectile", at = @At("RETURN"))
	private static ItemStack vl$skipAmmo(ItemStack original) {
		if (original.isEmpty()) {
			var item = VLBridge.getInfiniteArrows.get();

			if (!item.isEmpty()) {
				return item;
			}
		}

		return original;
	}
}
