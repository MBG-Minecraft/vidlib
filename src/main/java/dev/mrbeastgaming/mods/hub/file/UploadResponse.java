package dev.mrbeastgaming.mods.hub.file;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mrbeastgaming.mods.hub.api.data.HubUploadResponseEntry;

import java.util.List;

public record UploadResponse(
	List<HubUploadResponseEntry> files,
	long httpBodyLimit
) {
	public static final Codec<UploadResponse> CODEC = RecordCodecBuilder.create(i -> i.group(
		HubUploadResponseEntry.CODEC.listOf().fieldOf("files").forGetter(UploadResponse::files),
		Codec.LONG.optionalFieldOf("http_body_limit", 0L).forGetter(UploadResponse::httpBodyLimit)
	).apply(i, UploadResponse::new));
}
