package dev.latvian.mods.replay.api;

import net.minecraft.core.RegistryAccess;
import net.minecraft.util.Mth;
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

	default long getCurrentUTC() {
		var level = getLevel();

		if (level == null) {
			return -1L;
		}

		var tick = level.getGameTime();
		var fileInfo = getFileInfo();
		var startTick = fileInfo.getStartGameTick();
		var endTick = fileInfo.getEndGameTick();

		if (startTick == -1L || endTick == 1L || startTick >= endTick) {
			return -1L;
		}

		double delta = (double) (tick - startTick) / (double) (endTick - startTick);

		var startUTC = fileInfo.getStartUTC();
		var endUTC = fileInfo.getEndUTC();

		if (startUTC == -1L || endUTC == 1L || startUTC >= endUTC) {
			return -1L;
		}

		return startUTC + Mth.floor(delta * (endUTC - startUTC));
	}
}
