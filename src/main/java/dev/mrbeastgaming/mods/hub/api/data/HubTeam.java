package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.util.Hex32;
import dev.latvian.mods.klib.util.UInt64;

import java.time.Instant;

public record HubTeam(
	Hex32 id,
	String name,
	Instant created,
	String code,
	UInt64 discordRole,
	HubDataMap customData
) implements HubDBObject {
	public static final Codec<HubTeam> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HubDBObject.ID_FIELD.forGetter(HubTeam::id),
		Codec.STRING.optionalFieldOf("name", "").forGetter(HubTeam::name),
		KLibCodecs.INSTANT.optionalFieldOf("created", Instant.EPOCH).forGetter(HubTeam::created),
		Codec.STRING.optionalFieldOf("code", "").forGetter(HubTeam::code),
		UInt64.CODEC.optionalFieldOf("discord_guild", UInt64.NONE).forGetter(HubTeam::discordRole),
		HubDataMap.CODEC.optionalFieldOf("custom_data", HubDataMap.EMPTY).forGetter(HubTeam::customData)
	).apply(instance, HubTeam::new));
}