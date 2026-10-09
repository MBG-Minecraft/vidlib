package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.io.checksum.Checksum;

import java.time.Instant;

public record HubUploadRequestFile(
	String id,
	Checksum checksum,
	long size,
	String name,
	Instant created,
	Instant lastModified
) {
	public static final Codec<HubUploadRequestFile> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.STRING.optionalFieldOf("id", "").forGetter(HubUploadRequestFile::id),
		Checksum.CODEC.fieldOf("checksum").forGetter(HubUploadRequestFile::checksum),
		Codec.LONG.fieldOf("size").forGetter(HubUploadRequestFile::size),
		Codec.STRING.optionalFieldOf("name", "").forGetter(HubUploadRequestFile::name),
		KLibCodecs.INSTANT.fieldOf("created").forGetter(HubUploadRequestFile::created),
		KLibCodecs.INSTANT.fieldOf("last_modified").forGetter(HubUploadRequestFile::lastModified)
	).apply(i, HubUploadRequestFile::new));
}
