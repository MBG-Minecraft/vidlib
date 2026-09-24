package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Hex32;

import java.util.Optional;

public record HubOrigin(Hex32 ip, Optional<HubCountry> country) {
	public static final HubOrigin UNKNOWN = new HubOrigin(Hex32.NONE, Optional.empty());

	public static final MapCodec<HubOrigin> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		Hex32.LENIENT_CODEC.optionalFieldOf("ip", Hex32.NONE).forGetter(HubOrigin::ip),
		HubCountry.CODEC.optionalFieldOf("country").forGetter(HubOrigin::country)
	).apply(i, (ip, country) -> ip.raw() == 0 && country.isEmpty() ? UNKNOWN : new HubOrigin(ip, country)));
}
