package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record HubClientSessionRequest(
	String projectToken,
	boolean minecraftProfile,
	String identity
) {
	public static final Codec<HubClientSessionRequest> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.optionalFieldOf("project_token", "").forGetter(HubClientSessionRequest::projectToken),
		Codec.BOOL.optionalFieldOf("minecraft_profile", false).forGetter(HubClientSessionRequest::minecraftProfile),
		Codec.STRING.optionalFieldOf("identity", "").forGetter(HubClientSessionRequest::identity)
	).apply(instance, HubClientSessionRequest::new));
}
