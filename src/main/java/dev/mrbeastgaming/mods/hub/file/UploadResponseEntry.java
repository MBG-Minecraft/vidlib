package dev.mrbeastgaming.mods.hub.file;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.io.checksum.Checksum;

public record UploadResponseEntry(
	String id,
	Checksum checksum,
	long size,
	String token,
	long offset,
	Checksum offsetChecksum
) {
	public static final Codec<UploadResponseEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.STRING.optionalFieldOf("id", "").forGetter(UploadResponseEntry::id),
		Checksum.CODEC.fieldOf("checksum").forGetter(UploadResponseEntry::checksum),
		Codec.LONG.fieldOf("size").forGetter(UploadResponseEntry::size),
		Codec.STRING.fieldOf("token").forGetter(UploadResponseEntry::token),
		Codec.LONG.fieldOf("offset").forGetter(UploadResponseEntry::offset),
		Checksum.CODEC.fieldOf("checksum").forGetter(UploadResponseEntry::offsetChecksum)
	).apply(i, UploadResponseEntry::new));
}
