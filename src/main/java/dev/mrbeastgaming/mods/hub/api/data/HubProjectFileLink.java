package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.io.checksum.Checksum;

public record HubProjectFileLink(
	Checksum checksum,
	HubProjectFileLinkInfo info
) {
	public static final Codec<HubProjectFileLink> CODEC = RecordCodecBuilder.create(i -> i.group(
		Checksum.CODEC.fieldOf("checksum").forGetter(HubProjectFileLink::checksum),
		HubProjectFileLinkInfo.MAP_CODEC.forGetter(HubProjectFileLink::info)
	).apply(i, HubProjectFileLink::new));
}