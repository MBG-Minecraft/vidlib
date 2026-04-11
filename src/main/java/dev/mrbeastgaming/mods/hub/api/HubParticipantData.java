package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;

import java.time.Instant;
import java.util.Optional;

public record HubParticipantData(
	HubUserData user,
	Instant registryDate,
	Optional<HubMinecraftProfileData> minecraftProfile,
	Optional<HubRobloxProfileData> robloxProfile
) {
	public static final Codec<HubParticipantData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HubUserData.CODEC.fieldOf("user").forGetter(HubParticipantData::user),
		KLibCodecs.ISO_INSTANT.fieldOf("registry_date").forGetter(HubParticipantData::registryDate),
		HubMinecraftProfileData.CODEC.optionalFieldOf("minecraft_profile").forGetter(HubParticipantData::minecraftProfile),
		HubRobloxProfileData.CODEC.optionalFieldOf("roblox_profile").forGetter(HubParticipantData::robloxProfile)
	).apply(instance, HubParticipantData::new));
}
