package dev.latvian.mods.vidlib.core.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	@ModifyConstant(method = "lambda$static$1", constant = @Constant(intValue = 99))
	private static int vl$maxSlotSize(int original) {
		return 1_000_000_000;
	}
}
