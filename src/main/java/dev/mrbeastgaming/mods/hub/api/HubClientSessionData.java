package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;

import java.net.URI;
import java.util.Optional;

public record HubClientSessionData(
	URI gateway,
	HubUserData user,
	HubProjectData project,
	Optional<HubParticipantData> participant,
	HubUserCapabilities capabilities
) {
	public static HubClientSessionData CURRENT = null;

	public static final Codec<HubClientSessionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		KLibCodecs.URI.fieldOf("gateway").forGetter(HubClientSessionData::gateway),
		HubUserData.CODEC.fieldOf("user").forGetter(HubClientSessionData::user),
		HubProjectData.CODEC.fieldOf("project").forGetter(HubClientSessionData::project),
		HubParticipantData.CODEC.optionalFieldOf("participant").forGetter(HubClientSessionData::participant),
		HubUserCapabilities.CODEC.fieldOf("capabilities").forGetter(HubClientSessionData::capabilities)
	).apply(instance, HubClientSessionData::new));
}
