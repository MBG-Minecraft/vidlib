package dev.mrbeastgaming.mods.hub.file;

import dev.latvian.mods.klib.io.FileInfo;
import dev.mrbeastgaming.mods.hub.HubProjectConfig;
import org.jetbrains.annotations.Nullable;

public interface FileNameProvider {
	@Nullable
	String getFileName(FileInfo fileInfo, HubProjectConfig projectConfig) throws Exception;
}
