package dev.beast.mods.shimmer.feature.cutscene;

import dev.beast.mods.shimmer.math.worldnumber.WorldNumberContext;

@FunctionalInterface
public interface CutsceneTick {
	void tick(WorldNumberContext ctx);
}
