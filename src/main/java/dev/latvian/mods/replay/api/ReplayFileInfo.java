package dev.latvian.mods.replay.api;

import com.mojang.authlib.GameProfile;
import dev.latvian.mods.vidlib.feature.misc.PlatformModInfo;
import dev.latvian.mods.vidlib.util.PackSyncMeta;
import net.minecraft.Util;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
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

	default int getTotalTicks() {
		long start = getStartGameTick();
		long end = getEndGameTick();

		if (start == -1L || end == 1L || start >= end) {
			return 0;
		}

		return (int) (end - start);
	}

	default long getStartUTC() {
		return -1L;
	}

	default long getEndUTC() {
		return -1L;
	}

	default Duration getTotalDuration() {
		long start = getStartUTC();
		long end = getEndUTC();

		if (start == -1L || end == 1L || start >= end) {
			return Duration.ZERO;
		}

		return Duration.ofMillis(end - start);
	}

	default PackSyncMeta getPackSyncInfo() {
		return PackSyncMeta.EMPTY;
	}

	default List<PlatformModInfo> getModInfo() {
		return List.of();
	}
}
