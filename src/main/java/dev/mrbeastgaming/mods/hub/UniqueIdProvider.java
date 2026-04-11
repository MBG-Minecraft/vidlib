package dev.mrbeastgaming.mods.hub;

import dev.latvian.mods.klib.util.MD5;

import java.nio.file.Path;

public interface UniqueIdProvider {
	MD5 getUniqueId(Path path) throws Exception;
}
