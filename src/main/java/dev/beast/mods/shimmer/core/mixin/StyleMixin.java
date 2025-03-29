package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerStyle;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Style.class)
public class StyleMixin implements ShimmerStyle {
}
