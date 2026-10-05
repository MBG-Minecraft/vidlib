package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mrbeastgaming.mods.hub.api.data.HubKeys;

public record HubServerSessionRequest(
	boolean dedicatedServer,
	String projectToken,
	HubKeys keys,
	String identity
) {
	public static final Codec<HubServerSessionRequest> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.BOOL.optionalFieldOf("dedicated_server", false).forGetter(HubServerSessionRequest::dedicatedServer),
		Codec.STRING.optionalFieldOf("project_token", "").forGetter(HubServerSessionRequest::projectToken),
		HubKeys.CODEC.optionalFieldOf("keys", HubKeys.NONE).forGetter(HubServerSessionRequest::keys),
		Codec.STRING.optionalFieldOf("identity", "").forGetter(HubServerSessionRequest::identity)
	).apply(instance, HubServerSessionRequest::new));
}
