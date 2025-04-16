package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Component.class)
public interface ComponentMixin extends VLComponent {
}
