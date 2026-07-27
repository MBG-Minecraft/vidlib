package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.knumber.KNumberVariables;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.clock.Clock;
import dev.latvian.mods.vidlib.feature.clock.ClockValue;
import dev.latvian.mods.vidlib.feature.clock.SyncClocksPayload;
import dev.latvian.mods.vidlib.feature.data.SyncServerDataPayload;
import dev.latvian.mods.vidlib.feature.session.SessionData;
import dev.latvian.mods.vidlib.feature.zone.ZoneContainer;
import dev.latvian.mods.vidlib.feature.zone.ZoneVolume;
import dev.latvian.mods.vidlib.util.PauseType;
import dev.latvian.mods.vidlib.util.RepeatingTask;
import dev.latvian.mods.vidlib.util.ScheduledTask;
import net.minecraft.resources.Identifier;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
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

	default void schedule(int delay, Runnable task) {
		vl$getScheduledTaskHandler().run(delay, new RepeatingTask.WrappedRunnable(task));
	}

	default void scheduleRepeating(int delay, RepeatingTask task) {
		vl$getScheduledTaskHandler().run(delay, task);
	}

	default void removeZone(Ref<ZoneContainer> zone, int index) {
		throw new NoMixinException(this);
	}

	default void updateZone(Ref<ZoneContainer> zone, int index, ZoneVolume zoneVolume) {
		throw new NoMixinException(this);
	}

	default void vl$clearProfileCache() {
	}

	default KNumberVariables globalVariables() {
		return KNumberVariables.EMPTY;
	}

	default void syncGlobalVariables() {
	}

	default Collection<? extends SessionData> vl$getAllSessionData() {
		return List.of();
	}

	@ApiStatus.Internal
	default void sync(VLS2CPacketConsumer packets) {
		getDataMap().syncAll(packets, (uuid, updates) -> new SyncServerDataPayload(updates));
		packets.s2c(new SyncClocksPayload(vl$getClocks()));
	}

	default Map<Ref<Clock>, ClockValue> vl$getClocks() {
		throw new NoMixinException(this);
	}

	@Nullable
	default String getServerBrand() {
		return null;
	}
}
