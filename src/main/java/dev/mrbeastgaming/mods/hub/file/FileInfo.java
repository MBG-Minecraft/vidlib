package dev.mrbeastgaming.mods.hub.file;

import dev.latvian.mods.klib.io.IOUtils;

import java.nio.file.Path;

public record FileInfo(Path path, String name, long size) {
	public FileInfo(Path file) {
		this(file, file.getFileName().toString(), IOUtils.getSize(file));
	}

	public FileInfo(Path root, Path file) {
		this(file, root.relativize(file).toString().replace('\\', '/'), IOUtils.getSize(file));
	}
}
