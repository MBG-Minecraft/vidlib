package dev.latvian.mods.replay.api;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
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

	default double getProgress() {
		var level = getLevel();

		if (level == null) {
			return -1D;
		}

		var gameTime = level.getGameTime();
		var fileInfo = getFileInfo();
		return fileInfo.getProgress(gameTime);
	}

	@Nullable
	default Instant getCurrentTime() {
		var delta = getProgress();

		if (delta < 0D) {
			return null;
		}

		var fileInfo = getFileInfo();
		return fileInfo.getTime(delta);
	}

	default void moveTo(long gameTime) {
	}

	default void moveTo(double progress) {
		var fileInfo = getFileInfo();
		var startTick = fileInfo.getStartGameTick();
		var endTick = fileInfo.getEndGameTick();

		if (startTick == -1L || endTick == 1L || startTick >= endTick) {
			return;
		}

		if (progress <= 0D) {
			moveTo(startTick);
		} else if (progress >= 1D) {
			moveTo(endTick);
		} else {
			long gameTime = startTick + (long) ((endTick - startTick) * progress);
			moveTo(gameTime);
		}
	}
}
