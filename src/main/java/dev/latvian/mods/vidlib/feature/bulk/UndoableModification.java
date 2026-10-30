package dev.latvian.mods.vidlib.feature.bulk;

import dev.latvian.mods.klib.bulk.BlockModificationConsumer;
import net.minecraft.world.level.Level;

public interface UndoableModification {
	void undo(Level level, BlockModificationConsumer consumer);
}
