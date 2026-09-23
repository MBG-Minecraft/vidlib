package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mrbeastgaming.mods.hub.api.data.HubKeys;

public record HubServerSessionRequest(
	boolean dedicatedServer,
	String projectToken,
	HubKeys keys
) {
	public static final Codec<HubServerSessionRequest> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.BOOL.fieldOf("dedicated_server").forGetter(HubServerSessionRequest::dedicatedServer),
		Codec.STRING.fieldOf("project_token").forGetter(HubServerSessionRequest::projectToken),
		HubKeys.CODEC.fieldOf("keys").forGetter(HubServerSessionRequest::keys)
	).apply(instance, HubServerSessionRequest::new));
}
