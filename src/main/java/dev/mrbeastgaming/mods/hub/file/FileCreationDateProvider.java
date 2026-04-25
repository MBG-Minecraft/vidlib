package dev.mrbeastgaming.mods.hub.file;

import dev.latvian.mods.klib.io.FileInfo;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public interface FileCreationDateProvider {
	@Nullable
	Instant getFileCreated(FileInfo fileInfo) throws Exception;
}
