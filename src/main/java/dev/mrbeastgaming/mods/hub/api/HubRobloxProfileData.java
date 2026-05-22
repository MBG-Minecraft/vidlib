package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Hex32;
import dev.latvian.mods.klib.util.UInt64;

public record HubRobloxProfileData(
	Hex32 id,
	UInt64 uid,
	String name,
	String nickname,
	String preferredName,
	String picture
) {
	public static final Codec<HubRobloxProfileData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Hex32.CODEC.fieldOf("id").forGetter(HubRobloxProfileData::id),
		UInt64.CODEC.fieldOf("uid").forGetter(HubRobloxProfileData::uid),
		Codec.STRING.fieldOf("name").forGetter(HubRobloxProfileData::name),
		Codec.STRING.fieldOf("nickname").forGetter(HubRobloxProfileData::nickname),
		Codec.STRING.fieldOf("preferred_name").forGetter(HubRobloxProfileData::preferredName),
		Codec.STRING.fieldOf("picture").forGetter(HubRobloxProfileData::picture)
	).apply(instance, HubRobloxProfileData::new));
}
