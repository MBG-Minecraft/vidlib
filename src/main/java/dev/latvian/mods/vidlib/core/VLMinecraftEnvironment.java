package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.util.PauseType;
import dev.latvian.mods.vidlib.util.ScheduledTask;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface VLMinecraftEnvironment extends VLPlayerContainer, VLMinecraftEnvironmentDataHolder {
	@Override
	default VLMinecraftEnvironment getEnvironment() {
		return this;
	}

	default PauseType getPauseType() {
		return PauseType.NONE;
	}

	default void vl$execute(Runnable task) {
		((ReentrantBlockableEventLoop<Runnable>) this).execute(task);
	}

	default void vl$executeBlocking(Runnable task) {
		((ReentrantBlockableEventLoop<Runnable>) this).executeBlocking(task);
	}

	default CompletableFuture<Void> vl$submit(Runnable future) {
		return ((ReentrantBlockableEventLoop<Runnable>) this).submit(future);
	}

	default <T> CompletableFuture<T> vl$submit(Supplier<T> future) {
		return ((ReentrantBlockableEventLoop<Runnable>) this).submit(future);
	}

	@ApiStatus.Internal
	default void vl$preTick(PauseType paused) {
	}

	@ApiStatus.Internal
	default void vl$postTick(PauseType paused) {
	}

	default ScheduledTask.Handler vl$getScheduledTaskHandler() {
		throw new NoMixinException(this);
	}

	default void schedule(int ticks, Runnable task) {
		vl$getScheduledTaskHandler().run(ticks, task, false);
	}

	default void scheduleSafely(int ticks, Runnable task) {
		vl$getScheduledTaskHandler().run(ticks, task, true);
	}

	default void removeZone(UUID uuid) {
		throw new NoMixinException(this);
	}
}
