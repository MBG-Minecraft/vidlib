package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record HubFullData(
	HubKeyData userKeys,
	HubKeyData botKeys,
	List<HubCountry> countries
) {
	public static final Codec<HubFullData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HubKeyData.CODEC.fieldOf("user_keys").forGetter(HubFullData::userKeys),
		HubKeyData.CODEC.fieldOf("bot_keys").forGetter(HubFullData::botKeys),
		HubCountry.CODEC.listOf().fieldOf("countries").forGetter(HubFullData::countries)
	).apply(instance, HubFullData::new));
}
