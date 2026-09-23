package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Hex32;
import dev.latvian.mods.klib.util.UInt64;
import dev.mrbeastgaming.mods.hub.api.HubAPI;

import java.net.URI;
import java.util.Optional;

public record HubUser(
	Hex32 id,
	HubUserFlags flags,
	String name,
	Optional<URI> avatarUrl,
	UInt64 discordId
) implements HubDBObject {
	public static final Codec<HubUser> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HubDBObject.ID_FIELD.forGetter(HubUser::id),
		HubUserFlags.CODEC.optionalFieldOf("flags", HubUserFlags.NONE).forGetter(HubUser::flags),
		Codec.STRING.optionalFieldOf("name", "").forGetter(HubUser::name),
		HubAPI.URI_BASE_CODEC.optionalFieldOf("avatar_url").forGetter(HubUser::avatarUrl),
		UInt64.CODEC.optionalFieldOf("discord_id", UInt64.NONE).forGetter(HubUser::discordId)
	).apply(instance, HubUser::new));

	@Override
	public String toString() {
		return name + "#" + id;
	}

	public HubUser withFlags(HubUserFlags flags) {
		return new HubUser(id, flags, name, avatarUrl, discordId);
	}
}
