package dev.mrbeastgaming.mods.hub.api.gateway;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mrbeastgaming.mods.hub.api.HubUserDisplayData;

public record HubWorldUploadRequestData(
	String token,
	String worldId,
	String path,
	HubUserDisplayData sendingTo
) {
	public static final Codec<HubWorldUploadRequestData> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.STRING.fieldOf("token").forGetter(HubWorldUploadRequestData::token),
		Codec.STRING.fieldOf("world_id").forGetter(HubWorldUploadRequestData::worldId),
		Codec.STRING.fieldOf("path").forGetter(HubWorldUploadRequestData::path),
		HubUserDisplayData.CODEC.fieldOf("sending_to").forGetter(HubWorldUploadRequestData::sendingTo)
	).apply(i, HubWorldUploadRequestData::new));
}
