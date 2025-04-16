package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLLevel;
import dev.latvian.mods.vidlib.feature.bulk.UndoableModification;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Mixin(Level.class)
public abstract class LevelMixin implements VLLevel {
	@Unique
	private final List<UndoableModification> vl$undoable = new ArrayList<>();

	@Unique
	private final AtomicLong vl$nextPacketId = new AtomicLong(0L);

	@Override
	public List<UndoableModification> vl$getUndoableModifications() {
		return vl$undoable;
	}

	@Override
	public long vl$nextPacketId() {
		return vl$nextPacketId.incrementAndGet();
	}
}
