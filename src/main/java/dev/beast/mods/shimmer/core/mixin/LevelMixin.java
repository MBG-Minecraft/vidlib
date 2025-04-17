package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerLevel;
import dev.beast.mods.shimmer.feature.bulk.UndoableModificationHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Mixin(Level.class)
public abstract class LevelMixin implements ShimmerLevel {
	@Unique
	private final List<UndoableModificationHolder> shimmer$undoable = new ArrayList<>();

	@Unique
	private final AtomicLong shimmer$nextPacketId = new AtomicLong(0L);

	@Override
	public List<UndoableModificationHolder> shimmer$getUndoableModifications() {
		return shimmer$undoable;
	}

	@Override
	public long shimmer$nextPacketId() {
		return shimmer$nextPacketId.incrementAndGet();
	}
}
