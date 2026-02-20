package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLItemFrame;
import net.minecraft.world.entity.decoration.ItemFrame;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemFrame.class)
public abstract class ItemFrameMixin implements VLItemFrame {
}
