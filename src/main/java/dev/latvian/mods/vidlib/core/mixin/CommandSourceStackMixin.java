package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLCommandSourceStack;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CommandSourceStack.class)
public class CommandSourceStackMixin implements VLCommandSourceStack {
}
