package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.io.checksum.Checksum;
import dev.latvian.mods.klib.io.checksum.NoChecksum;

public record HubStoredFile(
	Checksum checksum,
	long size,
	HubUserAction created,
	HubUserAction modified
) {
	public static final HubStoredFile UNKNOWN = new HubStoredFile(NoChecksum.INSTANCE, 0L, HubUserAction.UNKNOWN, HubUserAction.UNKNOWN);

	public static final Codec<HubStoredFile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Checksum.CODEC.optionalFieldOf("checksum", NoChecksum.INSTANCE).forGetter(HubStoredFile::checksum),
		Codec.LONG.optionalFieldOf("size", 0L).forGetter(HubStoredFile::size),
		HubUserAction.CODEC.optionalFieldOf("created", HubUserAction.UNKNOWN).forGetter(HubStoredFile::created),
		HubUserAction.CODEC.optionalFieldOf("modified", HubUserAction.UNKNOWN).forGetter(HubStoredFile::modified)
	).apply(instance, HubStoredFile::new));
}
