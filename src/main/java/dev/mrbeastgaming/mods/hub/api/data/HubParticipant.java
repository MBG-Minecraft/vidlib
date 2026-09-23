package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.util.Hex32;

import java.time.Instant;
import java.util.Optional;

public record HubParticipant(
	Hex32 id,
	Instant registryDate,
	Optional<HubMinecraftProfile> minecraftProfile,
	Optional<HubRobloxProfile> robloxProfile,
	HubDataMap formData,
	HubDataMap customData
) implements HubDBObject {
	public static final Codec<HubParticipant> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HubDBObject.ID_FIELD.forGetter(HubParticipant::id),
		KLibCodecs.INSTANT.optionalFieldOf("registry_date", Instant.EPOCH).forGetter(HubParticipant::registryDate),
		HubMinecraftProfile.CODEC.optionalFieldOf("minecraft_profile").forGetter(HubParticipant::minecraftProfile),
		HubRobloxProfile.CODEC.optionalFieldOf("roblox_profile").forGetter(HubParticipant::robloxProfile),
		HubDataMap.CODEC.optionalFieldOf("form_data", HubDataMap.EMPTY).forGetter(HubParticipant::formData),
		HubDataMap.CODEC.optionalFieldOf("custom_data", HubDataMap.EMPTY).forGetter(HubParticipant::customData)
	).apply(instance, HubParticipant::new));
}
