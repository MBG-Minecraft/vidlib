package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.io.checksum.Checksum;
import dev.latvian.mods.klib.io.checksum.NoChecksum;

public record HubStoredFile(
	Checksum checksum,
	long size,
	HubFileAttribute created,
	HubFileAttribute modified
) {
	public static final HubStoredFile UNKNOWN = new HubStoredFile(NoChecksum.INSTANCE, 0L, HubFileAttribute.DEFAULT, HubFileAttribute.DEFAULT);

	public static final Codec<HubStoredFile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Checksum.CODEC.optionalFieldOf("checksum", NoChecksum.INSTANCE).forGetter(HubStoredFile::checksum),
		Codec.LONG.optionalFieldOf("size", 0L).forGetter(HubStoredFile::size),
		HubFileAttribute.CODEC.optionalFieldOf("created", HubFileAttribute.DEFAULT).forGetter(HubStoredFile::created),
		HubFileAttribute.CODEC.optionalFieldOf("modified", HubFileAttribute.DEFAULT).forGetter(HubStoredFile::modified)
	).apply(instance, HubStoredFile::new));
}
