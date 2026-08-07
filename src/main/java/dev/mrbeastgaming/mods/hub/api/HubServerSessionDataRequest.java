package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record HubServerSessionDataRequest(
	String projectToken,
	HubKeyData keys
) {
	public static final Codec<HubServerSessionDataRequest> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("project_token").forGetter(HubServerSessionDataRequest::projectToken),
		HubKeyData.CODEC.fieldOf("keys").forGetter(HubServerSessionDataRequest::keys)
	).apply(instance, HubServerSessionDataRequest::new));
}
