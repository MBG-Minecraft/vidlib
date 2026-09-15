package dev.mrbeastgaming.mods.hub.file;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record UploadResponse(
	List<UploadResponseEntry> files,
	long httpBodyLimit
) {
	public static final Codec<UploadResponse> CODEC = RecordCodecBuilder.create(i -> i.group(
		UploadResponseEntry.CODEC.listOf().fieldOf("files").forGetter(UploadResponse::files),
		Codec.LONG.optionalFieldOf("http_body_limit", 0L).forGetter(UploadResponse::httpBodyLimit)
	).apply(i, UploadResponse::new));
}
