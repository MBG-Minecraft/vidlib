package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.knumber.KNumberVariables;
import dev.latvian.mods.vidlib.feature.session.PlayerSessionData;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface VLMinecraftEnvironment extends VLPlayerContainer, VLMinecraftEnvironmentDataHolder {
	@Override
	default VLMinecraftEnvironment getEnvironment() {
		return this;
	}

	default KNumberVariables globalVariables() {
		return KNumberVariables.EMPTY;
	}

	default Collection<? extends PlayerSessionData> vl$getAllSessionData() {
		return List.of();
	}

	@Nullable
	default String getServerBrand() {
		return null;
	}
}
