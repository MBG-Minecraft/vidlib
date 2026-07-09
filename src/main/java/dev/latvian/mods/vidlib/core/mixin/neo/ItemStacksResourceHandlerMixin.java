package dev.latvian.mods.vidlib.core.mixin.neo;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ItemStacksResourceHandler.class)
public class ItemStacksResourceHandlerMixin {
	@ModifyConstant(method = "getCapacity(ILnet/neoforged/neoforge/transfer/item/ItemResource;)I", constant = @Constant(intValue = Item.ABSOLUTE_MAX_STACK_SIZE))
	private int vl$maxSlotSize(int original) {
		return 1_000_000_000;
	}
}
