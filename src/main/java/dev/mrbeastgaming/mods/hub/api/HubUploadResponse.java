package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mrbeastgaming.mods.hub.api.data.HubUploadResponseEntry;

import java.util.List;

public record HubUploadResponse(
	List<HubUploadResponseEntry> files,
	long httpBodyLimit
) {
	public static final Codec<HubUploadResponse> CODEC = RecordCodecBuilder.create(i -> i.group(
		HubUploadResponseEntry.CODEC.listOf().fieldOf("files").forGetter(HubUploadResponse::files),
		Codec.LONG.optionalFieldOf("http_body_limit", 0L).forGetter(HubUploadResponse::httpBodyLimit)
	).apply(i, HubUploadResponse::new));
}
