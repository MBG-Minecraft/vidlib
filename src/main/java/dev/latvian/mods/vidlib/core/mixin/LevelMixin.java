package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLLevel;
import dev.latvian.mods.vidlib.feature.bulk.UndoableModificationHolder;
import dev.latvian.mods.vidlib.util.PauseType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Mixin(Level.class)
public abstract class LevelMixin implements VLLevel {
	@Unique
	private final List<UndoableModificationHolder> vl$undoable = new ArrayList<>();

	@Unique
	private final AtomicLong vl$nextPacketId = new AtomicLong(0L);

	@Unique
	private List<Entity> vl$bosses = List.of();

	@Unique
	private Entity vl$mainBoss = null;

	@Override
	public void vl$preTick(PauseType paused) {
		vl$bosses = new ArrayList<>(1);
		vl$mainBoss = null;

		for (var entity : allEntities()) {
			if (entity.isMainBoss() && entity.isAlive()) {
				vl$bosses.add(entity);

				if (vl$mainBoss == null) {
					vl$mainBoss = entity;
				}
			}
		}
	}

	@Override
	public List<UndoableModificationHolder> vl$getUndoableModifications() {
		return vl$undoable;
	}

	@Override
	public long vl$nextPacketId() {
		return vl$nextPacketId.incrementAndGet();
	}

	@Override
	public List<Entity> getBosses() {
		return vl$bosses;
	}

	@Override
	@Nullable
	public Entity getMainBoss() {
		return vl$mainBoss;
	}
}
