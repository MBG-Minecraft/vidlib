package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.io.checksum.Checksum;
import dev.latvian.mods.klib.util.Hex32;

import java.util.Map;

public record HubProjectFileLinkResponse(
	Map<Checksum, Hex32> uploads
) {
	public static final Codec<HubProjectFileLinkResponse> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.unboundedMap(Checksum.CODEC, Hex32.CODEC).optionalFieldOf("uploads", Map.of()).forGetter(HubProjectFileLinkResponse::uploads)
	).apply(i, HubProjectFileLinkResponse::new));
}