package dev.mrbeastgaming.mods.hub.file;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.io.checksum.Checksum;

public record ChecksumPath(Checksum checksum, String path) {
	public static final Codec<ChecksumPath> CODEC = RecordCodecBuilder.create(i -> i.group(
		Checksum.CODEC.fieldOf("checksum").forGetter(ChecksumPath::checksum),
		Codec.STRING.fieldOf("path").forGetter(ChecksumPath::path)
	).apply(i, ChecksumPath::new));
}
