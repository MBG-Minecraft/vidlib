package dev.latvian.mods.vidlib;

import dev.latvian.mods.klib.CommonPaths;
import dev.latvian.mods.klib.platform.PlatformHelper;
import dev.latvian.mods.klib.util.Lazy;

import java.nio.file.Path;

public interface VidLibPaths {
	Lazy<Path> GAME = Lazy.of(() -> PlatformHelper.INSTANCE.getGameDirectory().resolve("vidlib"));
	Lazy<Path> LOCAL = CommonPaths.LOCAL.<Path>map(path -> path.resolve("vidlib"));
	Lazy<Path> USER = CommonPaths.USER.<Path>map(path -> path.resolve("vidlib"));
	Lazy<Path> ACTOR_RECORDINGS = LOCAL.<Path>map(path -> path.resolve("actor-recordings"));
}
