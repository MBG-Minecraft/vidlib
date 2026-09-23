package dev.mrbeastgaming.mods.hub.api.gateway;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Hex32;
import dev.mrbeastgaming.mods.hub.api.data.HubDBObject;
import dev.mrbeastgaming.mods.hub.api.data.HubDisplayContext;

public record HubWorldUploadRequest(
	HubDisplayContext ctx,
	String token,
	String worldId,
	String path,
	Hex32 sendingTo
) {
	public static final Codec<HubWorldUploadRequest> CODEC = RecordCodecBuilder.create(i -> i.group(
		HubDisplayContext.MAP_CODEC.forGetter(HubWorldUploadRequest::ctx),
		Codec.STRING.fieldOf("token").forGetter(HubWorldUploadRequest::token),
		Codec.STRING.fieldOf("world_id").forGetter(HubWorldUploadRequest::worldId),
		Codec.STRING.fieldOf("path").forGetter(HubWorldUploadRequest::path),
		HubDBObject.idCodec("sending_to").forGetter(HubWorldUploadRequest::sendingTo)
	).apply(i, HubWorldUploadRequest::new));
}
