package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Level.class)
public abstract class LevelMixin implements VLLevel {
}
