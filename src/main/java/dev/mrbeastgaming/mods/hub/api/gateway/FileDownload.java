package dev.mrbeastgaming.mods.hub.api.gateway;

import dev.latvian.mods.klib.io.checksum.Checksum;

import java.nio.file.Path;

public record FileDownload(
	Checksum checksum,
	long size,
	String path,
	String url,
	Path filePath
) {
}
