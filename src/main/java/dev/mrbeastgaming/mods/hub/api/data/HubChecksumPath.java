package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.io.checksum.Checksum;

public record HubChecksumPath(Checksum checksum, String path) {
	public static final Codec<HubChecksumPath> CODEC = RecordCodecBuilder.create(i -> i.group(
		Checksum.CODEC.fieldOf("checksum").forGetter(HubChecksumPath::checksum),
		Codec.STRING.fieldOf("path").forGetter(HubChecksumPath::path)
	).apply(i, HubChecksumPath::new));
}
