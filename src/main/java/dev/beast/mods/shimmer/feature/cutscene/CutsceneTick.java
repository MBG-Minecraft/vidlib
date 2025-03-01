package dev.beast.mods.shimmer.feature.cutscene;

import net.minecraft.world.level.Level;

@FunctionalInterface
public interface CutsceneTick {
	void tick(Level level, float progress);
}
