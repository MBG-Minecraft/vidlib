package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Hex32;

public record HubOrigin(Hex32 ip, HubCountry country) {
	public static final HubOrigin UNKNOWN = new HubOrigin(Hex32.NONE, HubCountry.UNKNOWN);

	public static final MapCodec<HubOrigin> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		Hex32.LENIENT_CODEC.optionalFieldOf("ip", Hex32.NONE).forGetter(HubOrigin::ip),
		HubCountry.CODEC.optionalFieldOf("country", HubCountry.UNKNOWN).forGetter(HubOrigin::country)
	).apply(i, (ip, country) -> ip.raw() == 0 && country.isUnknown() ? UNKNOWN : new HubOrigin(ip, country)));
}
