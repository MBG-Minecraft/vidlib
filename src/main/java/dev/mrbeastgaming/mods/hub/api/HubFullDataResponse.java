package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mrbeastgaming.mods.hub.api.data.HubCountry;
import dev.mrbeastgaming.mods.hub.api.data.HubDisplayContext;
import dev.mrbeastgaming.mods.hub.api.data.HubKeys;

import java.util.List;

public record HubFullDataResponse(
	HubDisplayContext ctx,
	HubKeys keys,
	List<HubCountry> countries
) {
	public static final Codec<HubFullDataResponse> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HubDisplayContext.MAP_CODEC.forGetter(HubFullDataResponse::ctx),
		HubKeys.CODEC.fieldOf("keys").forGetter(HubFullDataResponse::keys),
		HubCountry.CODEC.listOf().fieldOf("countries").forGetter(HubFullDataResponse::countries)
	).apply(instance, HubFullDataResponse::new));
}
