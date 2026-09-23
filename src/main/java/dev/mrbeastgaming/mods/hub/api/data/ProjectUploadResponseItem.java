package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.io.checksum.Checksum;
import dev.latvian.mods.klib.io.checksum.NoChecksum;
import dev.mrbeastgaming.mods.hub.api.HubAPI;

import java.net.URI;

public record ProjectUploadResponseItem(
	Checksum uniqueId,
	Checksum checksum,
	String name,
	URI url,
	long offset,
	int maxChunkSize
) {
	public static final Codec<ProjectUploadResponseItem> CODEC = RecordCodecBuilder.create(i -> i.group(
		Checksum.CODEC.optionalFieldOf("unique_id", NoChecksum.INSTANCE).forGetter(ProjectUploadResponseItem::uniqueId),
		Checksum.CODEC.fieldOf("checksum").forGetter(ProjectUploadResponseItem::checksum),
		Codec.STRING.optionalFieldOf("name", "").forGetter(ProjectUploadResponseItem::name),
		HubAPI.URI_BASE_CODEC.fieldOf("url").forGetter(ProjectUploadResponseItem::url),
		Codec.LONG.optionalFieldOf("offset", 0L).forGetter(ProjectUploadResponseItem::offset),
		Codec.INT.optionalFieldOf("max_chunk_size", 52428800).forGetter(ProjectUploadResponseItem::maxChunkSize)
	).apply(i, ProjectUploadResponseItem::new));

	@Override
	public String toString() {
		return uniqueId.isNil() ? (name + " (c/" + checksum + ")") : (name + "(u/" + uniqueId + ")");
	}
}
