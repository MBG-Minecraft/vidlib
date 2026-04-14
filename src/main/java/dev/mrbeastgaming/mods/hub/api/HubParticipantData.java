package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.util.Hex32;

import java.time.Instant;
import java.util.Optional;

public record HubParticipantData(
	Hex32 id,
	Instant registryDate,
	Optional<HubMinecraftProfileData> minecraftProfile,
	Optional<HubRobloxProfileData> robloxProfile,
	HubDataMap formData,
	HubDataMap customData
) {
	public static final Codec<HubParticipantData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Hex32.CODEC.fieldOf("id").forGetter(HubParticipantData::id),
		KLibCodecs.ISO_INSTANT.fieldOf("registry_date").forGetter(HubParticipantData::registryDate),
		HubMinecraftProfileData.CODEC.optionalFieldOf("minecraft_profile").forGetter(HubParticipantData::minecraftProfile),
		HubRobloxProfileData.CODEC.optionalFieldOf("roblox_profile").forGetter(HubParticipantData::robloxProfile),
		HubDataMap.CODEC.optionalFieldOf("form_data", HubDataMap.EMPTY).forGetter(HubParticipantData::formData),
		HubDataMap.CODEC.optionalFieldOf("custom_data", HubDataMap.EMPTY).forGetter(HubParticipantData::customData)
	).apply(instance, HubParticipantData::new));
}
