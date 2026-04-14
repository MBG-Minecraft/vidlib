package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Hex32;
import dev.latvian.mods.klib.util.UInt64;

public record HubUserData(
	Hex32 id,
	String name,
	String avatarUrl,
	UInt64 discordId,
	HubUserFlags flags
) {
	public static final Codec<HubUserData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Hex32.CODEC.fieldOf("id").forGetter(HubUserData::id),
		Codec.STRING.optionalFieldOf("name", "").forGetter(HubUserData::name),
		Codec.STRING.optionalFieldOf("avatar_url", "").forGetter(HubUserData::avatarUrl),
		UInt64.CODEC.optionalFieldOf("discord_id", UInt64.NONE).forGetter(HubUserData::discordId),
		HubUserFlags.CODEC.optionalFieldOf("flags", HubUserFlags.EMPTY).forGetter(HubUserData::flags)
	).apply(instance, HubUserData::new));
}
