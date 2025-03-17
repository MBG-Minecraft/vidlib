package dev.beast.mods.shimmer.feature.bulk;

import net.minecraft.world.level.Level;

public interface UndoableModification {
	void undo(Level level, BlockModificationConsumer consumer);
}
