package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Component.class)
public class ComponentMixin implements ShimmerComponent {
}
