package dev.latvian.mods.replay.api;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface ReplaySession {
	@Nullable
	<T extends ReplaySessionData> T getOptionalData(ReplaySessionDataType<T> type);

	default <T extends ReplaySessionData> T getData(ReplaySessionDataType<T> type) {
		return Objects.requireNonNull(getOptionalData(type));
	}

	ReplayFileInfo getFileInfo();

	Level getLevel();

	default RegistryAccess getRegistryAccess() {
		return getLevel().registryAccess();
	}
}
