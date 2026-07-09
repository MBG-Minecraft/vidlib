package dev.latvian.mods.vidlib.core.mixin.neo;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.items.ComponentItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin({ItemStackHandler.class, ComponentItemHandler.class})
public class ItemStackHandlerMixin {
	@ModifyConstant(method = "getSlotLimit", constant = @Constant(intValue = Item.ABSOLUTE_MAX_STACK_SIZE))
	private int vl$maxSlotSize(int original) {
		return 1_000_000_000;
	}
}
