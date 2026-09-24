package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mrbeastgaming.mods.hub.api.data.HubResponseContext;
import dev.mrbeastgaming.mods.hub.api.data.ProjectUploadResponseItem;

import java.util.List;

public record ProjectUploadResponse(
	HubResponseContext ctx,
	int maxChunkSize,
	List<ProjectUploadResponseItem> files
) {
	public static final Codec<ProjectUploadResponse> CODEC = RecordCodecBuilder.create(i -> i.group(
		HubResponseContext.MAP_CODEC.forGetter(ProjectUploadResponse::ctx),
		Codec.INT.fieldOf("max_chunk_size").forGetter(ProjectUploadResponse::maxChunkSize),
		ProjectUploadResponseItem.CODEC.listOf().optionalFieldOf("files", List.of()).forGetter(ProjectUploadResponse::files)
	).apply(i, ProjectUploadResponse::new));
}