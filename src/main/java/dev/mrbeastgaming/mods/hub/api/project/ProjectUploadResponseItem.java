package dev.mrbeastgaming.mods.hub.api.project;

import dev.latvian.mods.klib.util.MD5;

public record ProjectUploadResponseItem(
	MD5 uniqueId,
	MD5 checksum,
	String name,
	String url,
	long offset,
	int maxChunkSize
) {
	@Override
	public String toString() {
		return uniqueId.isEmpty() ? (name + " (c/" + checksum + ")") : (name + "(u/" + uniqueId + ")");
	}
}
