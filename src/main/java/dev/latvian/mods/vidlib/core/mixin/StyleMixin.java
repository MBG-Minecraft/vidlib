package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLStyle;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Style.class)
public class StyleMixin implements VLStyle {
}
