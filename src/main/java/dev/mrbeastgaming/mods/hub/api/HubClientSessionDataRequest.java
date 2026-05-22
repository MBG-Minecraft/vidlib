package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record HubClientSessionDataRequest(
	String projectToken,
	boolean minecraftProfile
) {
	public static final Codec<HubClientSessionDataRequest> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("project_token").forGetter(HubClientSessionDataRequest::projectToken),
		Codec.BOOL.optionalFieldOf("minecraft_profile", false).forGetter(HubClientSessionDataRequest::minecraftProfile)
	).apply(instance, HubClientSessionDataRequest::new));
}
