package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.io.checksum.Checksum;
import dev.latvian.mods.klib.io.checksum.NoChecksum;
import dev.mrbeastgaming.mods.hub.api.HubAPI;

import java.net.URI;

public record HubProjectUploadResponseItem(
	Checksum uniqueId,
	Checksum checksum,
	String name,
	URI url,
	long offset,
	int maxChunkSize
) {
	public static final Codec<HubProjectUploadResponseItem> CODEC = RecordCodecBuilder.create(i -> i.group(
		Checksum.CODEC.optionalFieldOf("unique_id", NoChecksum.INSTANCE).forGetter(HubProjectUploadResponseItem::uniqueId),
		Checksum.CODEC.fieldOf("checksum").forGetter(HubProjectUploadResponseItem::checksum),
		Codec.STRING.optionalFieldOf("name", "").forGetter(HubProjectUploadResponseItem::name),
		HubAPI.URI_BASE_CODEC.fieldOf("url").forGetter(HubProjectUploadResponseItem::url),
		Codec.LONG.optionalFieldOf("offset", 0L).forGetter(HubProjectUploadResponseItem::offset),
		Codec.INT.optionalFieldOf("max_chunk_size", 52428800).forGetter(HubProjectUploadResponseItem::maxChunkSize)
	).apply(i, HubProjectUploadResponseItem::new));

	@Override
	public String toString() {
		return uniqueId.isNil() ? (name + " (c/" + checksum + ")") : (name + "(u/" + uniqueId + ")");
	}
}
