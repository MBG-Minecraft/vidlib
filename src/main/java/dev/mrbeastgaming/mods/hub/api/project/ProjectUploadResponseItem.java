package dev.mrbeastgaming.mods.hub.api.project;

import dev.latvian.mods.klib.util.MD5;

public record ProjectUploadResponseItem(
	MD5 uniqueId,
	MD5 checksum,
	String url,
	long offset,
	int maxChunkSize
) {
}
