package dev.mrbeastgaming.mods.hub.api.gateway;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mrbeastgaming.mods.hub.api.data.HubResponseContext;
import dev.mrbeastgaming.mods.hub.api.data.HubUser;

public record HubWorldUploadRequest(
	HubResponseContext ctx,
	String token,
	String worldId,
	String path,
	HubUser sendingTo
) {
	public static final Codec<HubWorldUploadRequest> CODEC = RecordCodecBuilder.create(i -> i.group(
		HubResponseContext.MAP_CODEC.forGetter(HubWorldUploadRequest::ctx),
		Codec.STRING.fieldOf("token").forGetter(HubWorldUploadRequest::token),
		Codec.STRING.fieldOf("world_id").forGetter(HubWorldUploadRequest::worldId),
		Codec.STRING.fieldOf("path").forGetter(HubWorldUploadRequest::path),
		HubUser.CODEC.fieldOf("sending_to").forGetter(HubWorldUploadRequest::sendingTo)
	).apply(i, HubWorldUploadRequest::new));
}
