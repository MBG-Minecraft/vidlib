package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.data.DataMap;
import dev.beast.mods.shimmer.util.ScheduledTask;
import org.jetbrains.annotations.ApiStatus;

public interface ShimmerMinecraftEnvironment extends ShimmerEntityContainer {
	@Override
	default ShimmerMinecraftEnvironment shimmer$getEnvironment() {
		return this;
	}

	default DataMap getServerData() {
		throw new NoMixinException();
	}

	@ApiStatus.Internal
	default void shimmer$preTick() {
	}

	@ApiStatus.Internal
	default void shimmer$postTick() {
	}

	default ScheduledTask.Handler shimmer$getScheduledTaskHandler() {
		throw new NoMixinException();
	}

	default void schedule(int ticks, Runnable task) {
		shimmer$getScheduledTaskHandler().run(ticks, task, false);
	}

	default void scheduleSafely(int ticks, Runnable task) {
		shimmer$getScheduledTaskHandler().run(ticks, task, true);
	}
}
