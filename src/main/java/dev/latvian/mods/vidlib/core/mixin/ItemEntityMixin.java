package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLItemEntity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin implements VLItemEntity {
}
