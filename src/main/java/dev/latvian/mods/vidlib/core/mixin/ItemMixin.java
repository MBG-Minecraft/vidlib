package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLItem;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public abstract class ItemMixin implements VLItem {
}
