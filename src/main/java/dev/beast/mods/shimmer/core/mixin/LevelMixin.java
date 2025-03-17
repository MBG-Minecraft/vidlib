package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerLevel;
import dev.beast.mods.shimmer.feature.bulk.UndoableModification;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@Mixin(Level.class)
public abstract class LevelMixin implements ShimmerLevel {
	@Unique
	private final List<UndoableModification> shimmer$undoable = new ArrayList<>();

	@Override
	public List<UndoableModification> shimmer$getUndoableModifications() {
		return shimmer$undoable;
	}
}
