package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.util.Hex32;
import dev.latvian.mods.klib.util.UInt64;

import java.time.Instant;

public record HubTeamData(
	Hex32 id,
	String name,
	Instant created,
	String code,
	UInt64 discordRole
) {
	public static final Codec<HubTeamData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Hex32.CODEC.fieldOf("id").forGetter(HubTeamData::id),
		Codec.STRING.optionalFieldOf("name", "").forGetter(HubTeamData::name),
		KLibCodecs.ISO_INSTANT.fieldOf("created").forGetter(HubTeamData::created),
		Codec.STRING.optionalFieldOf("code", "").forGetter(HubTeamData::code),
		UInt64.CODEC.optionalFieldOf("discord_guild", UInt64.NONE).forGetter(HubTeamData::discordRole)
	).apply(instance, HubTeamData::new));
}