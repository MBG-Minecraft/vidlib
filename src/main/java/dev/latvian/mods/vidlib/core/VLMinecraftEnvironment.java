package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.knumber.KNumberVariables;
import dev.latvian.mods.klib.net.S2CPacketConsumer;
import dev.latvian.mods.klib.util.PauseType;
import dev.latvian.mods.vidlib.feature.data.SyncServerDataPayload;
import dev.latvian.mods.vidlib.feature.session.PlayerSessionData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface VLMinecraftEnvironment extends VLPlayerContainer, VLMinecraftEnvironmentDataHolder {
	@Override
	default VLMinecraftEnvironment getEnvironment() {
		return this;
	}

	default PauseType getPauseType() {
		return PauseType.NONE;
	}

	@ApiStatus.Internal
	default void vl$postTick(PauseType paused) {
	}

	default KNumberVariables globalVariables() {
		return KNumberVariables.EMPTY;
	}

	default Collection<? extends PlayerSessionData> vl$getAllSessionData() {
		return List.of();
	}

	@ApiStatus.Internal
	default void sync(S2CPacketConsumer packets) {
		getDataMap().syncAll(packets, (uuid, updates) -> new SyncServerDataPayload(updates));
	}

	@Nullable
	default String getServerBrand() {
		return null;
	}
}
