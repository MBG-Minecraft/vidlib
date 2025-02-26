package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.util.ScheduledTask;

public interface ShimmerMinecraftEnvironment {
	ScheduledTask.Handler shimmer$getScheduledTaskHandler();

	default void schedule(int ticks, Runnable task) {
		shimmer$getScheduledTaskHandler().run(ticks, task, false);
	}

	default void scheduleSafely(int ticks, Runnable task) {
		shimmer$getScheduledTaskHandler().run(ticks, task, true);
	}
}
