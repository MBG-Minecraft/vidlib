package dev.mrbeastgaming.mods.hub.api.gateway;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.mrbeastgaming.mods.hub.api.HubUserDisplayData;

import java.util.UUID;

public record HubWorldUploadRequestData(
	String token,
	UUID requestId,
	UUID sessionId,
	String worldId,
	String path,
	HubUserDisplayData sendingTo
) {
	public static final Codec<HubWorldUploadRequestData> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.STRING.fieldOf("token").forGetter(HubWorldUploadRequestData::token),
		KLibCodecs.UUID.fieldOf("request_id").forGetter(HubWorldUploadRequestData::requestId),
		KLibCodecs.UUID.fieldOf("session_id").forGetter(HubWorldUploadRequestData::sessionId),
		Codec.STRING.fieldOf("world_id").forGetter(HubWorldUploadRequestData::worldId),
		Codec.STRING.fieldOf("path").forGetter(HubWorldUploadRequestData::path),
		HubUserDisplayData.CODEC.fieldOf("sending_to").forGetter(HubWorldUploadRequestData::sendingTo)
	).apply(i, HubWorldUploadRequestData::new));
}
