package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.io.checksum.Checksum;
import dev.latvian.mods.klib.io.checksum.NoChecksum;

public record HubUploadResponseEntry(
	String id,
	Checksum checksum,
	long size,
	String token,
	long offset,
	Checksum offsetChecksum
) {
	public static final Codec<HubUploadResponseEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.STRING.optionalFieldOf("id", "").forGetter(HubUploadResponseEntry::id),
		Checksum.CODEC.fieldOf("checksum").forGetter(HubUploadResponseEntry::checksum),
		Codec.LONG.fieldOf("size").forGetter(HubUploadResponseEntry::size),
		Codec.STRING.fieldOf("token").forGetter(HubUploadResponseEntry::token),
		Codec.LONG.fieldOf("offset").forGetter(HubUploadResponseEntry::offset),
		Checksum.CODEC.optionalFieldOf("offset_checksum", NoChecksum.INSTANCE).forGetter(HubUploadResponseEntry::offsetChecksum)
	).apply(i, HubUploadResponseEntry::new));
}
