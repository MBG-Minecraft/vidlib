package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Hex32;
import dev.latvian.mods.klib.util.UInt64;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.ArrayList;

public record HubUserData(
	HubUserDisplayData display,
	UInt64 discordId,
	HubUserFlags flags
) {
	public static final Codec<HubUserData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HubUserDisplayData.MAP_CODEC.forGetter(HubUserData::display),
		UInt64.CODEC.optionalFieldOf("discord_id", UInt64.NONE).forGetter(HubUserData::discordId),
		HubUserFlags.CODEC.optionalFieldOf("flags", HubUserFlags.EMPTY).forGetter(HubUserData::flags)
	).apply(instance, HubUserData::new));

	public static final Codec<Int2ObjectMap<HubUserData>> INT_MAP_CODEC = CODEC.listOf().xmap(list -> {
		var map = new Int2ObjectLinkedOpenHashMap<HubUserData>();

		for (var user : list) {
			map.put(user.id().raw(), user);
		}

		return map;
	}, map -> new ArrayList<>(map.values()));

	public static HubUserData SELF = null;
	public static final Int2ObjectMap<HubUserData> KNOWN_USERS = new Int2ObjectOpenHashMap<>();

	public Hex32 id() {
		return display.id();
	}

	@Override
	public String toString() {
		return display.toString();
	}

	public HubUserData withFlags(HubUserFlags flags) {
		return new HubUserData(display, discordId, flags);
	}
}
