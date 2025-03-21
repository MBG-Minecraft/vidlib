package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.structure.StructureStorage;
import dev.beast.mods.shimmer.util.PauseType;
import dev.beast.mods.shimmer.util.ScheduledTask;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface ShimmerMinecraftEnvironment extends ShimmerEntityContainer, ShimmerMinecraftEnvironmentDataHolder {
	@Override
	default ShimmerMinecraftEnvironment shimmer$getEnvironment() {
		return this;
	}

	default PauseType getPauseType() {
		return PauseType.NONE;
	}

	default void shimmer$execute(Runnable task) {
		((ReentrantBlockableEventLoop<Runnable>) this).execute(task);
	}

	default void shimmer$executeBlocking(Runnable task) {
		((ReentrantBlockableEventLoop<Runnable>) this).executeBlocking(task);
	}

	default CompletableFuture<Void> shimmer$submit(Runnable future) {
		return ((ReentrantBlockableEventLoop<Runnable>) this).submit(future);
	}

	default <T> CompletableFuture<T> shimmer$submit(Supplier<T> future) {
		return ((ReentrantBlockableEventLoop<Runnable>) this).submit(future);
	}

	default StructureStorage shimmer$structureStorage() {
		throw new NoMixinException(this);
	}

	@ApiStatus.Internal
	default void shimmer$preTick(PauseType paused) {
	}

	@ApiStatus.Internal
	default void shimmer$postTick(PauseType paused) {
	}

	default ScheduledTask.Handler shimmer$getScheduledTaskHandler() {
		throw new NoMixinException(this);
	}

	default void schedule(int ticks, Runnable task) {
		shimmer$getScheduledTaskHandler().run(ticks, task, false);
	}

	default void scheduleSafely(int ticks, Runnable task) {
		shimmer$getScheduledTaskHandler().run(ticks, task, true);
	}
}
