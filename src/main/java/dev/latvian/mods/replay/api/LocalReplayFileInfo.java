package dev.latvian.mods.replay.api;

import dev.latvian.mods.klib.io.FileInfo;
import org.jetbrains.annotations.Nullable;

public interface LocalReplayFileInfo {
	FileInfo getFile();

	default boolean isRemote() {
		return false;
	}

	@Nullable
	default ReplayFileInfo getInfo() {
		return null;
	}

	default void open() {
	}
}
