package dev.latvian.mods.vidlib;

import dev.latvian.mods.common.CommonPaths;
import dev.latvian.mods.klib.util.Lazy;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public interface VidLibPaths {
	Lazy<Path> GAME = Lazy.of(() -> FMLPaths.GAMEDIR.get().resolve("vidlib"));
	Lazy<Path> LOCAL = CommonPaths.LOCAL.<Path>map(path -> path.resolve("vidlib"));
	Lazy<Path> USER = CommonPaths.USER.<Path>map(path -> path.resolve("vidlib"));
}
