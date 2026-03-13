package dev.latvian.mods.vidlib;

import dev.latvian.mods.common.CommonPaths;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;

import java.nio.file.Path;

public interface VidLibPaths {
	Lazy<Path> GAME = Lazy.of(() -> PlatformHelper.CURRENT.getGameDirectory().resolve("vidlib"));
	Lazy<Path> LOCAL = CommonPaths.LOCAL.<Path>map(path -> path.resolve("vidlib"));
	Lazy<Path> USER = CommonPaths.USER.<Path>map(path -> path.resolve("vidlib"));
}
