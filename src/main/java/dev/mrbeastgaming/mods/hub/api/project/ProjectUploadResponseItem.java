package dev.mrbeastgaming.mods.hub.api.project;

import dev.latvian.mods.klib.io.checksum.Checksum;

public record ProjectUploadResponseItem(
	Checksum uniqueId,
	Checksum checksum,
	String name,
	String url,
	long offset,
	int maxChunkSize
) {
	@Override
	public String toString() {
		return uniqueId.isNil() ? (name + " (c/" + checksum + ")") : (name + "(u/" + uniqueId + ")");
	}
}
