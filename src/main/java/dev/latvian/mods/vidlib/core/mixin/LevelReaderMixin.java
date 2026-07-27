package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLLevelReader;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelReader.class)
public interface LevelReaderMixin extends VLLevelReader {
}
