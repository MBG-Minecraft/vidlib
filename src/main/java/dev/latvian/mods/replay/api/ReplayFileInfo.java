package dev.latvian.mods.replay.api;

import com.mojang.authlib.GameProfile;
import dev.latvian.mods.vidlib.feature.misc.PlatformModInfo;
import dev.latvian.mods.vidlib.util.PackSyncMeta;
import net.minecraft.Util;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReplayFileInfo {
	default UUID getId() {
		return Util.NIL_UUID;
	}

	default String getFileName() {
		return "Unknown";
	}

	default long getFileSize() {
		return 0L;
	}

	default Util.OS getPlatform() {
		return Util.OS.UNKNOWN;
	}

	@Nullable
	default GameProfile getRecordedBy() {
		return null;
	}

	default String getWorldName() {
		return "";
	}

	default long getStartGameTick() {
		return -1L;
	}

	default long getEndGameTick() {
		return -1L;
	}

	default double getProgress(long gameTime) {
		var startTick = getStartGameTick();
		var endTick = getEndGameTick();

		if (startTick == -1L || endTick == 1L || startTick >= endTick) {
			return -1D;
		}

		return (double) (gameTime - startTick) / (double) (endTick - startTick);
	}

	default int getTotalTicks() {
		long start = getStartGameTick();
		long end = getEndGameTick();

		if (start == -1L || end == 1L || start >= end) {
			return 0;
		}

		return (int) (end - start);
	}

	@Nullable
	default Instant getStartTime() {
		return null;
	}

	@Nullable
	default Instant getEndTime() {
		return null;
	}

	@Nullable
	default Instant getTime(double delta) {
		var start = getStartTime();
		var end = getEndTime();

		if (start == null || end == null || !start.isBefore(end)) {
			return null;
		} else if (delta <= 0D) {
			return start;
		} else if (delta >= 1D) {
			return end;
		}

		try {
			long nanos = Duration.between(start, end).toNanos();
			return start.plusNanos((long) (nanos * delta));
		} catch (Exception ex) {
			return null;
		}
	}

	default Duration getTotalDuration() {
		var start = getStartTime();
		var end = getEndTime();

		if (start == null || end == null || end.isAfter(start)) {
			return Duration.ZERO;
		}

		return Duration.between(start, end);
	}

	default PackSyncMeta getPackSyncInfo() {
		return PackSyncMeta.EMPTY;
	}

	default List<PlatformModInfo> getModInfo() {
		return List.of();
	}

	default byte @Nullable [] getIconBytes() {
		return null;
	}
}
