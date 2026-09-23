package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Hex32;
import dev.latvian.mods.klib.util.UInt64;

public record HubRobloxProfile(
	Hex32 id,
	UInt64 uid,
	String name,
	String nickname,
	String preferredName,
	String picture
) implements HubDBObject {
	public static final Codec<HubRobloxProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HubDBObject.ID_FIELD.forGetter(HubRobloxProfile::id),
		UInt64.CODEC.fieldOf("uid").forGetter(HubRobloxProfile::uid),
		Codec.STRING.fieldOf("name").forGetter(HubRobloxProfile::name),
		Codec.STRING.fieldOf("nickname").forGetter(HubRobloxProfile::nickname),
		Codec.STRING.fieldOf("preferred_name").forGetter(HubRobloxProfile::preferredName),
		Codec.STRING.fieldOf("picture").forGetter(HubRobloxProfile::picture)
	).apply(instance, HubRobloxProfile::new));
}
