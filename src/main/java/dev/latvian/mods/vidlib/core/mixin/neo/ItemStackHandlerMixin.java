package dev.latvian.mods.vidlib.core.mixin.neo;

import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * 26.3 renamed NeoForge's {@code net.neoforged.neoforge.items.ItemStackHandler} /
 * {@code ComponentItemHandler} to {@code net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler}
 * and replaced {@code getSlotLimit} with {@code getCapacity}.
 */
@Mixin(ItemStacksResourceHandler.class)
public class ItemStackHandlerMixin {
	@ModifyConstant(method = "getCapacity", constant = @Constant(intValue = 99))
	private int vl$maxSlotSize(int original) {
		return 1_000_000_000;
	}
}
